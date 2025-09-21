# Database Documentation

## Overview

The Environmental Monitoring system uses PostgreSQL as its primary database, optimized for high-performance time-series data storage and retrieval. The schema is designed to handle large volumes of environmental data from multiple sources with efficient indexing strategies.

## Database Schema

### Core Tables

#### weather_data
Stores weather observations from National Weather Service stations.

```sql
CREATE TABLE weather_data (
    id BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    pressure DECIMAL(7,2),
    wind_speed DECIMAL(5,2),
    wind_direction INTEGER,
    visibility DECIMAL(5,2),
    weather_conditions TEXT,
    raw_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_weather_station_timestamp` - Composite index for station-specific time queries
- `idx_weather_timestamp` - Time-based queries for recent data
- `idx_weather_created_at` - Data ingestion monitoring

#### meteo_data
Open-Meteo weather forecast and historical data.

```sql
CREATE TABLE meteo_data (
    id BIGSERIAL PRIMARY KEY,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature_2m DECIMAL(5,2),
    relative_humidity_2m DECIMAL(5,2),
    precipitation DECIMAL(6,2),
    wind_speed_10m DECIMAL(5,2),
    wind_direction_10m INTEGER,
    uv_index DECIMAL(4,2),
    raw_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_meteo_location_timestamp` - Geographic and temporal queries
- `idx_meteo_timestamp` - Recent data retrieval
- `idx_meteo_location` - Location-based searches

#### air_quality_data
Air quality measurements from EPA monitoring stations.

```sql
CREATE TABLE air_quality_data (
    id BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    pm25 DECIMAL(6,2),
    pm10 DECIMAL(6,2),
    no2 DECIMAL(6,2),
    o3 DECIMAL(6,2),
    so2 DECIMAL(6,2),
    co DECIMAL(6,2),
    aqi INTEGER,
    raw_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_air_quality_station_timestamp` - Station-specific queries
- `idx_air_quality_timestamp` - Time-series analysis
- `idx_air_quality_location` - Geographic searches

#### marine_data
Tidal and oceanographic data from NOAA stations.

```sql
CREATE TABLE marine_data (
    id BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    water_level DECIMAL(6,2),
    wave_height DECIMAL(5,2),
    wave_period DECIMAL(5,2),
    wave_direction INTEGER,
    water_temperature DECIMAL(5,2),
    salinity DECIMAL(5,2),
    raw_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_marine_station_timestamp` - Station and time-based queries
- `idx_marine_timestamp` - Recent marine conditions
- `idx_marine_location` - Coastal area searches

#### fire_data
Forest fire tracking and incident management.

```sql
CREATE TABLE fire_data (
    id BIGSERIAL PRIMARY KEY,
    fire_id VARCHAR(100) UNIQUE,
    name VARCHAR(255),
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    discovery_date DATE,
    containment_date DATE,
    fire_size_acres DECIMAL(10,2),
    fire_cause VARCHAR(100),
    fire_status VARCHAR(50),
    incident_type VARCHAR(100),
    raw_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_fire_status` - Active fire queries
- `idx_fire_location` - Geographic proximity searches
- `idx_fire_discovery_date` - Historical fire analysis
- `idx_fire_updated_at` - Recent updates tracking

#### webcam_data
Public webcam monitoring and status tracking.

```sql
CREATE TABLE webcam_data (
    id BIGSERIAL PRIMARY KEY,
    webcam_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    description TEXT,
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    last_checked TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_webcam_active` - Active webcam filtering
- `idx_webcam_category_active` - Category-based searches
- `idx_webcam_location` - Full-text search on location

### System Tables

#### data_source_status
Monitoring and health tracking for external data sources.

```sql
CREATE TABLE data_source_status (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(100) NOT NULL UNIQUE,
    last_successful_fetch TIMESTAMP,
    last_error TIMESTAMP,
    error_message TEXT,
    is_active BOOLEAN DEFAULT true,
    fetch_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0
);
```

**Key Indexes:**
- `idx_data_source_active` - Active source filtering

#### dashboard_configs
User dashboard customization and layout preferences.

```sql
CREATE TABLE dashboard_configs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    config_name VARCHAR(255) NOT NULL,
    layout_config JSONB NOT NULL,
    data_sources JSONB NOT NULL,
    refresh_intervals JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Key Indexes:**
- `idx_dashboard_configs_user` - User-specific configurations

## Performance Optimizations

### Indexing Strategy

#### Time-Series Optimization
All primary data tables include optimized indexes for time-series queries:
- Composite indexes combining entity ID and timestamp
- Descending timestamp order for recent data queries
- Separate timestamp indexes for cross-table temporal analysis

#### Geographic Indexing
Location-based queries are optimized with:
- Composite lat/lon indexes for bounding box queries
- Individual coordinate indexes for range searches
- Spatial indexes for advanced geographic operations

#### Status and Category Indexing
Filtering operations optimized with:
- Boolean indexes for active/inactive status
- Category-based indexes for classification queries
- Full-text search indexes for location descriptions

### Query Optimization

#### Common Query Patterns

**Recent Data Queries:**
```sql
-- Optimized with idx_weather_timestamp
SELECT * FROM weather_data 
WHERE timestamp >= NOW() - INTERVAL '24 hours'
ORDER BY timestamp DESC;
```

**Station-Specific Historical Data:**
```sql
-- Optimized with idx_weather_station_timestamp
SELECT * FROM weather_data 
WHERE station_id = 'KORD' 
  AND timestamp BETWEEN '2024-01-01' AND '2024-01-31'
ORDER BY timestamp DESC;
```

**Geographic Proximity Searches:**
```sql
-- Optimized with idx_fire_location
SELECT * FROM fire_data 
WHERE latitude BETWEEN 40.0 AND 41.0
  AND longitude BETWEEN -88.0 AND -87.0
  AND fire_status = 'Active';
```

#### Performance Monitoring

**Query Execution Analysis:**
```sql
-- Enable query timing
\timing on

-- Analyze query plans
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM weather_data 
WHERE timestamp >= NOW() - INTERVAL '1 hour';
```

**Index Usage Statistics:**
```sql
-- Monitor index effectiveness
SELECT schemaname, tablename, attname, n_distinct, correlation
FROM pg_stats 
WHERE tablename IN ('weather_data', 'air_quality_data');
```

### Maintenance Operations

#### Index Maintenance
```sql
-- Rebuild indexes for optimal performance
REINDEX TABLE weather_data;

-- Update table statistics
ANALYZE weather_data;
```

#### Partition Strategy (Future Enhancement)
```sql
-- Example partition setup for large tables
CREATE TABLE weather_data_2024_01 PARTITION OF weather_data
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

## Data Retention Policies

### Automated Cleanup
```sql
-- Remove old weather data (>1 year)
DELETE FROM weather_data 
WHERE timestamp < NOW() - INTERVAL '1 year';

-- Archive old fire data
INSERT INTO fire_data_archive 
SELECT * FROM fire_data 
WHERE discovery_date < DATE '2023-01-01';
```

### Backup Strategy
```bash
# Daily backups
pg_dump -U postgres -h localhost -d enterprise > backup_$(date +%Y%m%d).sql

# Compressed backups
pg_dump -U postgres -h localhost -d enterprise | gzip > backup_$(date +%Y%m%d).sql.gz
```

## Database Configuration

### Connection Pooling (HikariCP)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

### PostgreSQL Tuning
```conf
# postgresql.conf optimizations
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
random_page_cost = 1.1
```

## Monitoring and Alerts

### Performance Metrics
```sql
-- Monitor table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Index usage statistics
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

### Health Checks
```sql
-- Connection monitoring
SELECT COUNT(*) as active_connections 
FROM pg_stat_activity 
WHERE state = 'active';

-- Lock monitoring
SELECT relation, mode, granted 
FROM pg_locks 
WHERE NOT granted;
```

## Migration Scripts

### Schema Updates
```sql
-- Add new column with default value
ALTER TABLE weather_data 
ADD COLUMN weather_severity INTEGER DEFAULT 0;

-- Create index concurrently (non-blocking)
CREATE INDEX CONCURRENTLY idx_weather_severity 
ON weather_data(weather_severity);
```

### Data Migration
```sql
-- Migrate data between schema versions
UPDATE weather_data 
SET weather_severity = 
  CASE 
    WHEN weather_conditions ILIKE '%storm%' THEN 3
    WHEN weather_conditions ILIKE '%rain%' THEN 2
    ELSE 1
  END;
```

## Security Considerations

### Access Control
```sql
-- Create read-only user for reporting
CREATE USER env_monitor_readonly WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE enterprise TO env_monitor_readonly;
GRANT USAGE ON SCHEMA public TO env_monitor_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO env_monitor_readonly;
```

### Data Protection
- Encrypted connections (SSL/TLS)
- Regular security updates
- Backup encryption
- Access logging and monitoring

## Troubleshooting

### Common Issues

**Slow Queries:**
```sql
-- Identify slow queries
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

**Index Bloat:**
```sql
-- Check index bloat
SELECT schemaname, tablename, indexname, 
       pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
ORDER BY pg_relation_size(indexrelid) DESC;
```

**Connection Issues:**
```bash
# Check PostgreSQL logs
tail -f /var/log/postgresql/postgresql-*.log

# Verify port and connections
netstat -an | grep 5432
```

### Performance Tuning Checklist
- [ ] Indexes created for all frequent query patterns
- [ ] Table statistics up to date (ANALYZE)
- [ ] Connection pooling properly configured
- [ ] Query plans reviewed for full table scans
- [ ] Partition strategy implemented for large tables
- [ ] Regular maintenance tasks scheduled