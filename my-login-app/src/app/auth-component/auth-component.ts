import { Component, inject, OnInit } from '@angular/core'; // 1. Ensure OnInit is imported here
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router'; 
import { AuthService } from '../services/auth';
import { AuthResponse } from '../models/auth-response.model'; // Import your interface model

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './auth-component.html',
  styleUrls: ['./auth-component.css'],
})
// 2. Added "implements OnInit" to fix the lifecycle warning
export class AuthComponent implements OnInit { 
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  authForm: FormGroup;
  isLoginMode = true;

  constructor() {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      name: [''],
    });
  }

  ngOnInit(): void {
    const customPreference = localStorage.getItem('prefer_login_mode');
    if (customPreference === 'false') {
      this.isLoginMode = false;
      this.authForm.get('name')?.setValidators([Validators.required]);
      this.authForm.get('name')?.updateValueAndValidity();
    }
    localStorage.removeItem('prefer_login_mode'); 
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
        // 3. Fixed: Changed response type from 'any' to 'AuthResponse'
        next: (response: AuthResponse) => {
          console.log('Login successful', response);
          this.router.navigate(['/dashboard']);
        },
        // 4. Fixed: Changed err type from 'any' to a structural validation block
        error: (err: { error?: { message?: string } }) => {
          console.error('Login failed', err);
          alert(err.error?.message || 'Login failed. Please check credentials.');
        },
      });
    } else {
      this.authService.signUp(this.authForm.value).subscribe({
        // 5. Fixed: Changed response type from 'any' to 'AuthResponse'
        next: (response: AuthResponse) => {
          console.log('Registration successful', response);
          this.router.navigate(['/dashboard']);
        },
        // 6. Fixed: Changed err type from 'any' to a structural validation block
        error: (err: { error?: { message?: string } }) => {
          console.error('Registration failed', err);
          alert(err.error?.message || 'Registration failed. Try again.');
        },
      });
    }
  }
}
