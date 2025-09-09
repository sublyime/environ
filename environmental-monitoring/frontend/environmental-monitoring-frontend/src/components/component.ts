// component.ts
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Component({
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