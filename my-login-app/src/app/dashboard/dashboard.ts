import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core'; // 🌟 1. Added ChangeDetectorRef here
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userName: string | null = 'Loading...';
  userToken: string | null = 'Loading...';

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  // 🌟 2. Inject ChangeDetectorRef (cdr) inside your class constructor parameters
  constructor() {}

  ngOnInit(): void {
    const activeToken = this.authService.getToken();

    if (this.authService.isLoggedIn() && activeToken) {
      this.fetchFreshDataFromBackend(activeToken);
    } else {
      this.router.navigate(['/auth']);
    }
  }

  fetchFreshDataFromBackend(token: string): void {
    try {
      const tokenParts: string[] = token.split('.');

      if (tokenParts.length < 2) {
        throw new Error('Invalid JWT format structure');
      }

      const base64Payload: string = tokenParts[1];
      const decodedString = atob(base64Payload);
      const tokenPayload = JSON.parse(decodedString);
      const userEmail = tokenPayload.sub;

      if (userEmail) {
        this.authService.getProfile(userEmail).subscribe({
          next: (profileData: any) => {
            this.userName = profileData.userName || profileData.username || 'User';
            this.userToken = profileData.token;

            console.log('TS updated name:', this.userName);
            console.log('TS updated token:', this.userToken);

            // 🌟 3. THE CRITICAL FIX: Forces Angular to repaint the template layout immediately
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('Failed to load profile from backend:', err);
            this.userName = 'Error loading name';
            this.userToken = 'Error loading token';
            this.cdr.detectChanges(); // 🌟 Force render on error state too
          },
        });
      }
    } catch (e) {
      console.error('Parsing context validation failed:', e);
      this.onLogout();
    }
  }

  isUserLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  navigateToAuth(loginMode: boolean): void {
    this.router.navigate(['/auth']).then(() => {
      localStorage.setItem('prefer_login_mode', loginMode ? 'true' : 'false');
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.userName = null;
    this.userToken = null;
    this.router.navigate(['/auth']);
  }
}
