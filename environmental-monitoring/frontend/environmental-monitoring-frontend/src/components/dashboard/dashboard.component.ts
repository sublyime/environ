import { Component, OnInit, OnDestroy } from '@angular/core';
import { DashboardService, DashboardData } from '../../services/dashboard.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  dashboardData: DashboardData | null = null;
  loading = true;
  error = '';
  lastUpdated = new Date();
  refreshInterval = 5; // minutes
  private subscription?: Subscription;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadDashboardData();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  loadDashboardData(): void {
    this.loading = true;
    this.dashboardService.getDashboardData(24).subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.lastUpdated = new Date();
        this.loading = false;
        this.error = '';
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.error = 'Failed to load dashboard data';
        this.loading = false;
      }
    });
  }

  startAutoRefresh(): void {
    this.subscription = interval(this.refreshInterval * 60 * 1000)
      .pipe(
        switchMap(() => this.dashboardService.getDashboardData(24))
      )
      .subscribe({
        next: (data) => {
          this.dashboardData = data;
          this.lastUpdated = new Date();
        },
        error: (error) => {
          console.error('Error during auto-refresh:', error);
        }
      });
  }

  refreshDataSource(dataSource: string): void {
    this.dashboardService.refreshDataSource(dataSource).subscribe({
      next: (response) => {
        console.log('Refresh initiated:', response);
        // Reload data after a short delay
        setTimeout(() => this.loadDashboardData(), 2000);
      },
      error: (error) => {
        console.error('Error refreshing data source:', error);
      }
    });
  }

  manualRefresh(): void {
    this.loadDashboardData();
  }
}
