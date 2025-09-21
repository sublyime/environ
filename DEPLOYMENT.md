# Deployment Guide

This guide covers deployment strategies for the Environmental Monitoring Dashboard across different environments.

## 🚀 Quick Start

### Local Development
```bash
# 1. Start PostgreSQL
sudo systemctl start postgresql

# 2. Create database and apply schema
createdb enterprise
psql -U postgres -d enterprise -f database/schema.sql

# 3. Start backend
cd backend
mvn spring-boot:run

# 4. Start frontend
cd frontend/environmental-monitoring-frontend
npm install
ng serve
```

Access the application at `http://localhost:4200`

## 🐳 Docker Deployment

### Docker Compose (Recommended for Development)

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: enterprise
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "5432:5432"
    networks:
      - env-monitor

  backend:
    build: ./backend
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/enterprise
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=password
    ports:
      - "8080:8080"
    depends_on:
      - database
    networks:
      - env-monitor

  frontend:
    build: ./frontend/environmental-monitoring-frontend
    ports:
      - "4200:80"
    depends_on:
      - backend
    networks:
      - env-monitor

volumes:
  postgres_data:

networks:
  env-monitor:
    driver: bridge
```

### Backend Dockerfile
Create `backend/Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile
Create `frontend/environmental-monitoring-frontend/Dockerfile`:
```dockerfile
FROM node:18-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist/environmental-monitoring-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### Nginx Configuration
Create `frontend/environmental-monitoring-frontend/nginx.conf`:
```nginx
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### Deploy with Docker Compose
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Remove volumes (destroys data)
docker-compose down -v
```

## ☁️ Cloud Deployment

### AWS ECS Deployment

#### Task Definition
```json
{
  "family": "env-monitor",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "your-repo/env-monitor-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://your-rds-endpoint:5432/enterprise"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/env-monitor",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### RDS Database Setup
```bash
# Create RDS PostgreSQL instance
aws rds create-db-instance \
  --db-instance-identifier env-monitor-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password SecurePassword123 \
  --allocated-storage 20 \
  --db-name enterprise
```

### Google Cloud Run

#### Backend Deployment
```bash
# Build and push to Google Container Registry
docker build -t gcr.io/PROJECT_ID/env-monitor-backend ./backend
docker push gcr.io/PROJECT_ID/env-monitor-backend

# Deploy to Cloud Run
gcloud run deploy env-monitor-backend \
  --image gcr.io/PROJECT_ID/env-monitor-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SPRING_DATASOURCE_URL=jdbc:postgresql://CLOUD_SQL_IP:5432/enterprise
```

#### Frontend Deployment
```bash
# Build and deploy to Firebase Hosting
cd frontend/environmental-monitoring-frontend
npm run build
firebase deploy --only hosting
```

### Azure Container Instances

```bash
# Create resource group
az group create --name env-monitor --location eastus

# Create container instance
az container create \
  --resource-group env-monitor \
  --name env-monitor-backend \
  --image your-registry/env-monitor-backend:latest \
  --dns-name-label env-monitor-api \
  --ports 8080 \
  --environment-variables \
    SPRING_DATASOURCE_URL=jdbc:postgresql://your-postgres:5432/enterprise
```

## 🔧 Production Configuration

### Environment Variables

Create `.env` file for production:
```bash
# Database
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=enterprise
DB_USERNAME=postgres
DB_PASSWORD=secure_password

# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# External APIs
WEATHER_API_KEY=your_api_key
AIR_QUALITY_API_KEY=your_api_key

# Monitoring
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_FAIRCHILD_ENVMONITOR=INFO
```

### Production Application Properties
Create `application-prod.yml`:
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/enterprise}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 50
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory

  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=15m

server:
  port: ${SERVER_PORT:8080}
  tomcat:
    threads:
      max: 300
      min-spare: 20
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json

logging:
  level:
    root: INFO
    com.fairchild.envmonitor: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

## 📊 Monitoring & Observability

### Prometheus Metrics
Add to `pom.xml`:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Grafana Dashboard
Create `grafana-dashboard.json`:
```json
{
  "dashboard": {
    "title": "Environmental Monitor",
    "panels": [
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "http_server_requests_seconds_sum / http_server_requests_seconds_count"
          }
        ]
      },
      {
        "title": "Database Connections",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active"
          }
        ]
      }
    ]
  }
}
```

### Health Checks
```yaml
# docker-compose.yml health checks
services:
  backend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  frontend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:80/"]
      interval: 30s
      timeout: 10s
      retries: 3
```

## 🔒 Security

### SSL/TLS Configuration
```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: env-monitor
  port: 8443
```

### Reverse Proxy (Nginx)
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;

    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 🔄 CI/CD Pipeline

### GitHub Actions
Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run backend tests
        run: |
          cd backend
          mvn test
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Run frontend tests
        run: |
          cd frontend/environmental-monitoring-frontend
          npm ci
          npm test -- --watch=false --browsers=ChromeHeadless

  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and push Docker images
        run: |
          docker build -t ${{ secrets.REGISTRY }}/env-monitor-backend:latest ./backend
          docker build -t ${{ secrets.REGISTRY }}/env-monitor-frontend:latest ./frontend/environmental-monitoring-frontend
          
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          
          docker push ${{ secrets.REGISTRY }}/env-monitor-backend:latest
          docker push ${{ secrets.REGISTRY }}/env-monitor-frontend:latest
      
      - name: Deploy to production
        run: |
          # Add your deployment commands here
          echo "Deploying to production..."
```

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Environment variables configured
- [ ] Database schema applied
- [ ] SSL certificates obtained
- [ ] Domain DNS configured
- [ ] Monitoring setup verified
- [ ] Backup strategy implemented
- [ ] Load testing completed

### Post-Deployment
- [ ] Health checks passing
- [ ] Application accessible
- [ ] Database connectivity verified
- [ ] External API connections working
- [ ] Monitoring alerts configured
- [ ] Performance metrics baseline established
- [ ] Log aggregation working
- [ ] Backup verification completed

## 🆘 Troubleshooting

### Common Deployment Issues

**Database Connection Errors**
```bash
# Test database connectivity
docker run --rm postgres:15 psql -h your-db-host -U postgres -d enterprise -c "SELECT version();"
```

**Container Startup Issues**
```bash
# Check container logs
docker logs container-name

# Debug container
docker exec -it container-name /bin/bash
```

**Network Connectivity**
```bash
# Test internal connectivity
docker exec backend-container curl -f http://database:5432

# Test external APIs
curl -f https://api.weather.gov/stations
```

### Performance Issues
```bash
# Monitor resource usage
docker stats

# Check application metrics
curl http://localhost:8080/api/actuator/metrics

# Database performance
docker exec database-container psql -U postgres -d enterprise -c "SELECT * FROM pg_stat_activity;"
```

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Deployment Guide](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Google Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Azure Container Instances](https://docs.microsoft.com/en-us/azure/container-instances/)