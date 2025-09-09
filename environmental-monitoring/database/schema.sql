-- Environmental Monitoring Database Schema
CREATE DATABASE enterprise;
\c enterprise;

-- Weather data from api.weather.gov
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

-- Open-Meteo weather data
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

-- Tidal and wave information
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

-- Public webcam information
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

-- Particulate monitoring data
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

-- Forest fire information
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

-- Dashboard configurations for customization
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

-- Data source monitoring
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

-- Create indexes for performance
CREATE INDEX idx_weather_data_timestamp ON weather_data(timestamp);
CREATE INDEX idx_weather_data_station ON weather_data(station_id);
CREATE INDEX idx_meteo_data_timestamp ON meteo_data(timestamp);
CREATE INDEX idx_meteo_data_location ON meteo_data(latitude, longitude);
CREATE INDEX idx_marine_data_timestamp ON marine_data(timestamp);
CREATE INDEX idx_marine_data_station ON marine_data(station_id);
CREATE INDEX idx_air_quality_timestamp ON air_quality_data(timestamp);
CREATE INDEX idx_air_quality_station ON air_quality_data(station_id);
CREATE INDEX idx_fire_data_location ON fire_data(latitude, longitude);
CREATE INDEX idx_dashboard_configs_user ON dashboard_configs(user_id);

-- Insert initial data source status records
INSERT INTO data_source_status (source_name, is_active) VALUES
('weather.gov', true),
('open-meteo', true),
('marine-data', true),
('webcams', true),
('air-quality', true),
('fire-data', true);

