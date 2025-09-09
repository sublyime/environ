import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  constructor(private http: HttpClient) { }

  getDashboardData(hours: number = 24): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.baseUrl}/dashboard/data?hours=${hours}`);
  }

  refreshDataSource(dataSource: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/dashboard/refresh/${dataSource}`, {});
  }
}
