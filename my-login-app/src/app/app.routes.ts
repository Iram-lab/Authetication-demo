import { Routes } from '@angular/router';
import { AuthComponent } from './auth-component/auth-component'; 
import { DashboardComponent } from './dashboard/dashboard';
import { authGuard } from './guards/auth-guard';
// 🌟 Import your functional guard

export const routes: Routes = [
  // 1. Send root website hits straight to /auth so login is visible first
  { path: '', redirectTo: '/auth', pathMatch: 'full' },
  
  // 2. Protected Dashboard - Now ONLY accessible if authGuard returns true
  { 
    path: 'dashboard', 
    component: DashboardComponent, 
    canActivate: [authGuard] // 🌟 Guard added here!
  },
  
  // 3. Dedicated authentication view panel
  { path: 'auth', component: AuthComponent },
  
  // Fallback catch-all route redirects back to login panel securely
  { path: '**', redirectTo: '/auth' }
];
