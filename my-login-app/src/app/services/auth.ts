import { Injectable, inject } from '@angular/core';

import { Observable, map } from 'rxjs';
import { AuthResponse } from '../models/auth-response.model';
import * as CryptoJS from 'crypto-js';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  private baseUrl = environment.apiUrl;
  private readonly SECRET_KEY = 'k9X#mP2vF!8zA5qW@7bN4cC9xZ1vB3nL';

  // Helper: Encrypt data using standard CryptoJS Passphrase mode
  // Fixed: Changed 'any' to 'unknown' to safely handle varying input objects or strings
  private encrypt(data: unknown): string {
    const plainText = typeof data === 'string' ? data : JSON.stringify(data);
    const encrypted = CryptoJS.AES.encrypt(plainText, this.SECRET_KEY);
    return encrypted.toString(); 
  }

  // Helper: Decrypt standard OpenSSL Base64 payload back to JSON or Text
  // Fixed: Changed return type 'any' to 'unknown' to enforce strict type checking upon retrieval
  private decrypt(ciphertext: string): unknown {
    const bytes = CryptoJS.AES.decrypt(ciphertext, this.SECRET_KEY);
    const decryptedText = bytes.toString(CryptoJS.enc.Utf8);
    try {
      return JSON.parse(decryptedText);
    } catch {
      return decryptedText;
    }
  }

  // Fixed: Replaced 'any' credentials with Record object type
  login(credentials: Record<string, unknown>): Observable<AuthResponse> {
    const encryptedPayload = this.encrypt(credentials);
    const body = { data: encryptedPayload };

    // Fixed: Defined wrapper response format explicitly instead of using 'any'
    return this.http.post<{ data: string }>(`${this.baseUrl}/login`, body).pipe(
      map((wrappedRes) => {
        const decryptedRes = this.decrypt(wrappedRes.data) as AuthResponse;
        this.setSession(decryptedRes);
        return decryptedRes;
      }),
    );
  }

  // Fixed: Replaced 'any' userData with Record object type
  signUp(userData: Record<string, unknown>): Observable<AuthResponse> {
    const encryptedPayload = this.encrypt(userData);
    const body = { data: encryptedPayload };

    // Fixed: Defined wrapper response format explicitly instead of using 'any'
    return this.http.post<{ data: string }>(`${this.baseUrl}/register`, body).pipe(
      map((wrappedRes) => {
        const decryptedRes = this.decrypt(wrappedRes.data) as AuthResponse;
        this.setSession(decryptedRes);
        return decryptedRes;
      }),
    );
  }

  getProfile(email: string): Observable<AuthResponse> {
    // Fixed: Defined wrapper response format explicitly instead of using 'any'
    return this.http.get<{ data: string }>(`${this.baseUrl}/profile?email=${encodeURIComponent(email)}`).pipe(
      map((wrappedRes) => {
        const decryptedRes = this.decrypt(wrappedRes.data) as AuthResponse;
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
