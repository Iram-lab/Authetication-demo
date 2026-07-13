// 📄 File: src/app/components/auth/auth.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

import { Router } from '@angular/router'; // 👈 1. Add this import
import { AuthService } from '../services/auth';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth-component.html',
  styleUrls: ['./auth-component.css']
})
export class AuthComponent {
  authForm: FormGroup;
  isLoginMode = true;

  // 2. Inject Router inside your constructor parameters
  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router 
  ) {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      name: ['']
    });
  }
ngOnInit(): void {
  const customPreference = localStorage.getItem('prefer_login_mode');
  if (customPreference === 'false') {
    this.isLoginMode = false;
    this.authForm.get('name')?.setValidators([Validators.required]);
    this.authForm.get('name')?.updateValueAndValidity();
  }
  localStorage.removeItem('prefer_login_mode'); // Clear hook state cache
}
  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.authForm.reset();
    const nameControl = this.authForm.get('name');
    if (!this.isLoginMode) {
      nameControl?.setValidators([Validators.required]);
    } else {
      nameControl?.clearValidators();
    }
    nameControl?.updateValueAndValidity();
  }

  onSubmit() {
    if (this.authForm.invalid) {
      return;
    }

    if (this.isLoginMode) {
      this.authService.login(this.authForm.value).subscribe({
        next: (response:any) => {
          console.log('Login successful', response);
          // 3. 🛑 REDIRECT HERE ON SUCCESSFUL LOGIN
          this.router.navigate(['/dashboard']); 
        },
        error: (err:any) => {
          console.error('Login failed', err);
          alert(err.error?.message || 'Login failed. Please check credentials.');
        }
      });
    } else {
      this.authService.signUp(this.authForm.value).subscribe({
        next: (response:any) => {
          console.log('Registration successful', response);
          // 4. 🛑 REDIRECT HERE ON SUCCESSFUL SIGNUP
          this.router.navigate(['/dashboard']); 
        },
        error: (err:any) => {
          console.error('Registration failed', err);
          alert(err.error?.message || 'Registration failed. Try again.');
        }
      });
    }
  }
}
