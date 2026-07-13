import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AuthResponse } from '../models/auth-response.model';
import * as CryptoJS from 'crypto-js';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  private baseUrl = environment.apiUrl;
  // private apiUrl = 'http://localhost:8080/api/auth';
  private readonly SECRET_KEY = 'k9X#mP2vF!8zA5qW@7bN4cC9xZ1vB3nL';

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {}

  // Helper: Encrypt data using standard CryptoJS Passphrase mode
  private encrypt(data: any): string {
    const plainText = typeof data === 'string' ? data : JSON.stringify(data);
    const encrypted = CryptoJS.AES.encrypt(plainText, this.SECRET_KEY);
    return encrypted.toString(); // Outputs a standard OpenSSL Base64 format string
  }

  // Helper: Decrypt standard OpenSSL Base64 payload back to JSON or Text
  private decrypt(ciphertext: string): any {
    const bytes = CryptoJS.AES.decrypt(ciphertext, this.SECRET_KEY);
    const decryptedText = bytes.toString(CryptoJS.enc.Utf8);
    try {
      return JSON.parse(decryptedText);
    } catch {
      return decryptedText;
    }
  }

  login(credentials: any): Observable<AuthResponse> {
    const encryptedPayload = this.encrypt(credentials);

    // 🌟 PRODUCTION APPROACH: Wrap ciphertext in JSON block object
    const body = { data: encryptedPayload };

    return this.http.post<any>(`${this.baseUrl}/login`, body).pipe(
      map((wrappedRes) => {
        // Unpack the JSON { data: "..." } structure coming from server response
        const decryptedRes: AuthResponse = this.decrypt(wrappedRes.data);
        this.setSession(decryptedRes);
        return decryptedRes;
      }),
    );
  }

  signUp(userData: any): Observable<AuthResponse> {
    const encryptedPayload = this.encrypt(userData);

    // 🌟 PRODUCTION APPROACH: Wrap ciphertext in JSON block object
    const body = { data: encryptedPayload };

    return this.http.post<any>(`${this.baseUrl}/register`, body).pipe(
      map((wrappedRes) => {
        // Unpack the JSON { data: "..." } structure coming from server response
        const decryptedRes: AuthResponse = this.decrypt(wrappedRes.data);
        this.setSession(decryptedRes);
        return decryptedRes;
      }),
    );
  }

  // Add this method to your existing AuthService class in auth.service.ts
  getProfile(email: string): Observable<AuthResponse> {
    // Pass the email parameter to fetch specific data
    return this.http.get<any>(`${this.baseUrl}/profile?email=${encodeURIComponent(email)}`).pipe(
      map((wrappedRes) => {
        // 🌟 Decrypt production wrapped response data dynamically
        const decryptedRes: AuthResponse = this.decrypt(wrappedRes.data);
        console.log(decryptedRes);
        return decryptedRes;
      }),
    );
  }
  private setSession(authResult: AuthResponse) {
    localStorage.setItem('auth_token', authResult.token);
    localStorage.setItem('user_name', authResult.userName);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_name');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
