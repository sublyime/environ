// component.ts
import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Component({
  selector: 'app-base-component',
  template: `
    <div [class]="layoutClass">
      <!-- content -->
    </div>
  `
})
export class MyComponent {
  layoutClass = '';
  
  constructor(private breakpointObserver: BreakpointObserver) {
    this.breakpointObserver.observe([
      Breakpoints.Handset,
      Breakpoints.Tablet,
      Breakpoints.Web
    ]).subscribe(result => {
      if (result.breakpoints[Breakpoints.Handset]) {
        this.layoutClass = 'flex-column';
      } else {
        this.layoutClass = 'flex-row';
      }
    });
  }
}