import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, catchError, shareReplay } from 'rxjs/operators';

export interface WeatherData {
  id: number;
  stationId: string;
  timestamp: string;
  temperature: number;
  humidity: number;
  pressure: number;
  windSpeed: number;
  windDirection: number;
  visibility: number;
  weatherConditions: string;
  createdAt: string;
}

export interface DashboardData {
  recentWeatherData: WeatherData[];
  recentMeteoData: any[];
  recentMarineData: any[];
  recentAirQualityData: any[];
  recentFireData: any[];
  activeWebcams: any[];
  dataSourceStatuses: any[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl = 'http://localhost:8080/api';
  private cache = new Map<string, Observable<any>>();
  private cacheTimeout = 5 * 60 * 1000; // 5 minutes

  constructor(private http: HttpClient) { }

  getDashboardData(hours: number = 24): Observable<DashboardData> {
    const cacheKey = `dashboard-${hours}`;
    
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }

    const request$ = this.http.get<DashboardData>(`${this.baseUrl}/dashboard/data?hours=${hours}`)
      .pipe(
        tap(() => console.log(`Dashboard data fetched for ${hours} hours`)),
        catchError(error => {
          console.error('Error fetching dashboard data:', error);
          throw error;
        }),
        shareReplay(1)
      );

    this.cache.set(cacheKey, request$);

    // Clear cache after timeout
    setTimeout(() => {
      this.cache.delete(cacheKey);
    }, this.cacheTimeout);

    return request$;
  }

  refreshDataSource(dataSource: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/dashboard/refresh/${dataSource}`, {})
      .pipe(
        tap(() => {
          // Clear cache when data source is refreshed
          this.clearCache();
        }),
        catchError(error => {
          console.error(`Error refreshing data source ${dataSource}:`, error);
          throw error;
        })
      );
  }

  private clearCache(): void {
    this.cache.clear();
  }
}
