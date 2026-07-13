import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core'; 
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userName: string | null = 'Loading...';
  userToken: string | null = 'Loading...';

  // Fixed: Removed the empty constructor block entirely to satisfy @typescript-eslint/no-empty-function

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
          // Fixed: Replaced 'any' with a structural object to satisfy @typescript-eslint/no-explicit-any
          next: (profileData: { userName?: string; username?: string; token?: string }) => {
            this.userName = profileData.userName || profileData.username || 'User';
            this.userToken = profileData.token || null;

            console.log('TS updated name:', this.userName);
            console.log('TS updated token:', this.userToken);

            this.cdr.detectChanges();
          },
          error: (err: unknown) => {
            console.error('Failed to load profile from backend:', err);
            this.userName = 'Error loading name';
            this.userToken = 'Error loading token';
            this.cdr.detectChanges(); 
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
