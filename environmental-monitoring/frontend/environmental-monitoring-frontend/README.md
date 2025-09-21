# Environmental Monitoring Frontend

Angular frontend for the Environmental Monitoring Dashboard - a real-time data visualization platform for environmental data aggregation.

## 🚀 Features

- **Real-time Dashboard**: Live environmental data with auto-refresh
- **Responsive Design**: Mobile-friendly interface
- **Performance Optimized**: OnPush change detection and client-side caching
- **Error Handling**: Robust error handling with retry mechanisms
- **Memory Efficient**: Proper subscription management and cleanup

## 🛠️ Technology Stack

- **Angular**: 17+ with TypeScript
- **RxJS**: Reactive programming for data streams
- **Bootstrap/Angular Material**: UI components
- **Chart.js/D3**: Data visualization
- **PWA Support**: Service worker for offline capability

## 📋 Prerequisites

- Node.js 18+ 
- npm 9+
- Angular CLI 17+

## 🔧 Installation & Setup

### Install Dependencies
```bash
npm install
```

### Development Server
```bash
ng serve
```
Navigate to `http://localhost:4200/`. The app will automatically reload when you change source files.

### Build for Production
```bash
ng build --configuration production
```
Build artifacts will be stored in the `dist/` directory.

## ⚙️ Configuration

### Environment Configuration

**src/environments/environment.ts** (Development)
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  refreshInterval: 5 * 60 * 1000, // 5 minutes
  cacheTimeout: 5 * 60 * 1000 // 5 minutes
};
```

**src/environments/environment.prod.ts** (Production)
```typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api',
  refreshInterval: 5 * 60 * 1000,
  cacheTimeout: 5 * 60 * 1000
};
```

## 🏗️ Application Structure

```
src/
├── app/
│   ├── components/
│   │   └── dashboard/              # Main dashboard component
│   ├── services/
│   │   └── dashboard.service.ts    # API service with caching
│   ├── models/                     # TypeScript interfaces
│   ├── guards/                     # Route guards
│   └── interceptors/               # HTTP interceptors
├── assets/                         # Static assets
├── environments/                   # Environment configurations
└── styles/                        # Global styles
```

## 📊 Key Components

### Dashboard Component
- **File**: `src/components/dashboard/dashboard.component.ts`
- **Features**: 
  - Real-time data fetching with auto-refresh
  - OnPush change detection for performance
  - Memory leak prevention with takeUntil pattern
  - Error handling with retry logic

### Dashboard Service
- **File**: `src/app/services/dashboard.service.ts`
- **Features**:
  - HTTP client with caching
  - Automatic cache invalidation
  - Error handling and retry mechanisms
  - Observable sharing to prevent duplicate requests

## 🎯 Performance Optimizations

### Change Detection Strategy
```typescript
@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush
})
```

### Memory Management
```typescript
private destroy$ = new BehaviorSubject<boolean>(false);

ngOnDestroy(): void {
  this.destroy$.next(true);
  this.destroy$.complete();
}

// Use takeUntil to prevent memory leaks
this.dataService.getData()
  .pipe(takeUntil(this.destroy$))
  .subscribe(data => {
    // Handle data
  });
```

### Client-Side Caching
```typescript
private cache = new Map<string, Observable<any>>();

getDashboardData(hours: number): Observable<DashboardData> {
  const cacheKey = `dashboard-${hours}`;
  
  if (this.cache.has(cacheKey)) {
    return this.cache.get(cacheKey)!;
  }
  
  const request$ = this.http.get<DashboardData>(`${this.baseUrl}/dashboard/data?hours=${hours}`)
    .pipe(shareReplay(1));
    
  this.cache.set(cacheKey, request$);
  return request$;
}
```

## 🧪 Testing

### Unit Tests
```bash
ng test
```
Executes unit tests via [Karma](https://karma-runner.github.io).

### End-to-End Tests
```bash
ng e2e
```
Executes end-to-end tests via your preferred e2e testing platform.

### Coverage Reports
```bash
ng test --code-coverage
```
Generates coverage reports in the `coverage/` directory.

## 🔧 Code Scaffolding

### Generate Component
```bash
ng generate component component-name
```

### Generate Service
```bash
ng generate service service-name
```

### Generate Module
```bash
ng generate module module-name
```

For a complete list of available schematics:
```bash
ng generate --help
```

## 📱 PWA Features

The application includes Progressive Web App capabilities:

- **Service Worker**: Offline functionality
- **App Manifest**: Install on mobile devices
- **Caching Strategies**: Network-first for API calls, cache-first for static assets

### Enable PWA
```bash
ng add @angular/pwa
```

## 🎨 Styling & UI

### Global Styles
- **Location**: `src/styles.scss`
- **Framework**: Bootstrap 5 + custom SCSS
- **Responsive**: Mobile-first design approach

### Component Styles
- **Methodology**: BEM (Block Element Modifier)
- **Preprocessing**: SCSS
- **Theming**: CSS custom properties for dark/light themes

## 📈 Performance Monitoring

### Bundle Analysis
```bash
ng build --stats-json
npx webpack-bundle-analyzer dist/stats.json
```

### Lighthouse Audits
- Run Chrome DevTools Lighthouse
- Monitor Performance, Accessibility, Best Practices, SEO scores
- Target: 90+ scores across all categories

### Angular DevTools
- Install Angular DevTools browser extension
- Monitor component tree and change detection
- Profile performance bottlenecks

## 🚀 Deployment

### GitHub Pages
```bash
ng deploy --base-href=/environ/
```

### Docker
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/* /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Production Checklist
- [ ] Environment variables configured
- [ ] API URLs updated for production
- [ ] Bundle optimization enabled
- [ ] Source maps disabled
- [ ] Service worker configured
- [ ] HTTPS enabled
- [ ] Content compression enabled

## 🔍 Debugging

### Development Tools
```typescript
// Enable Angular debugging
import { enableProdMode } from '@angular/platform-core';

if (environment.production) {
  enableProdMode();
} else {
  // Development debugging
  console.log('Development mode enabled');
}
```

### Common Issues

**API Connection Errors**
- Check if backend is running on correct port
- Verify CORS configuration
- Check browser network tab for request details

**Performance Issues**
- Use OnPush change detection
- Implement proper subscription cleanup
- Monitor for memory leaks with Chrome DevTools

**Build Errors**
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Update Angular CLI: `npm install -g @angular/cli@latest`
- Check for TypeScript version compatibility

## 🤝 Contributing

1. Follow Angular style guide
2. Write unit tests for new components
3. Update documentation for new features
4. Use conventional commit messages
5. Ensure accessibility compliance (WCAG 2.1)

## 📚 Further Resources

- [Angular Documentation](https://angular.io/docs)
- [RxJS Documentation](https://rxjs.dev/)
- [Angular DevKit](https://github.com/angular/angular-cli)
- [Angular Material](https://material.angular.io/)
- [Angular PWA](https://angular.io/guide/service-worker-intro)

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
