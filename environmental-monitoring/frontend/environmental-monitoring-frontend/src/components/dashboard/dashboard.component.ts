import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { DashboardService, DashboardData } from '../../app/services/dashboard.service';
import { interval, Subscription, BehaviorSubject, timer } from 'rxjs';
import { switchMap, shareReplay, takeUntil, startWith, retry, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new BehaviorSubject<boolean>(false);
  
  dashboardData$ = new BehaviorSubject<DashboardData | null>(null);
  loading$ = new BehaviorSubject<boolean>(true);
  error$ = new BehaviorSubject<string>('');
  lastUpdated$ = new BehaviorSubject<Date>(new Date());
  
  refreshInterval = 5; // minutes
  private refreshSubscription?: Subscription;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadDashboardData();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadDashboardData(): void {
    this.loading$.next(true);
    this.error$.next('');
    
    this.dashboardService.getDashboardData(24)
      .pipe(
        retry(3),
        catchError(error => {
          console.error('Error loading dashboard data:', error);
          this.error$.next('Failed to load dashboard data');
          this.loading$.next(false);
          return of(null);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data: DashboardData | null) => {
          if (data) {
            this.dashboardData$.next(data);
            this.lastUpdated$.next(new Date());
            this.error$.next('');
          }
          this.loading$.next(false);
        }
      });
  }

  startAutoRefresh(): void {
    this.refreshSubscription = timer(0, this.refreshInterval * 60 * 1000)
      .pipe(
        startWith(0),
        switchMap(() => this.dashboardService.getDashboardData(24)),
        retry(2),
        shareReplay(1),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data: DashboardData) => {
          this.dashboardData$.next(data);
          this.lastUpdated$.next(new Date());
        },
        error: (error: any) => {
          console.error('Error during auto-refresh:', error);
        }
      });
  }

  refreshDataSource(dataSource: string): void {
    this.dashboardService.refreshDataSource(dataSource)
      .pipe(
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response: any) => {
          console.log('Refresh initiated:', response);
          // Reload data after a short delay
          timer(2000).pipe(
            takeUntil(this.destroy$)
          ).subscribe(() => this.loadDashboardData());
        },
        error: (error: any) => {
          console.error('Error refreshing data source:', error);
        }
      });
  }

  manualRefresh(): void {
    this.loadDashboardData();
  }

  trackByIndex(index: number, item: any): number {
    return index;
  }

  // Helper methods for template
  getAqiClass(aqi: number): string {
    if (aqi <= 50) return 'aqi-good';
    if (aqi <= 100) return 'aqi-moderate';
    if (aqi <= 150) return 'aqi-unhealthy-sensitive';
    if (aqi <= 200) return 'aqi-unhealthy';
    if (aqi <= 300) return 'aqi-very-unhealthy';
    return 'aqi-hazardous';
  }

  getFireStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'active': return 'fire-active';
      case 'contained': return 'fire-contained';
      case 'controlled': return 'fire-controlled';
      default: return 'fire-unknown';
    }
  }

  openWebcam(url: string): void {
    window.open(url, '_blank');
  }
}
