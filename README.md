# Fabric Management System - Backend

A comprehensive Spring Boot backend application for managing fabric production operations with multi-machine product management, simplified shift assignments, piece tracking with analytics, and performance monitoring.

## Application Overview

The Fabric Management System backend provides a complete REST API solution for fabric manufacturing operations, featuring:

- **Multi-Machine Product Management**: Normalized database structure with `product_machines` junction table
- **Simplified Shift Assignment System**: Streamlined morning/evening shifts with machine-specific assignments
- **Comprehensive Piece Analytics**: Excel processing, date-wise analytics, and chart generation
- **Performance Monitoring**: Worker analytics with cost calculations and flexible date ranges
- **File Management**: Excel upload/download with validation and error handling

## Key Features

### Database Architecture
- **Normalized Product Structure**: `products` table with `product_machines` junction table
- **Simplified Shift System**: `shifts` and `worker_shift_assignments` with machine integration
- **Comprehensive Tracking**: `meters`, `pieces`, and analytics tables
- **User Management**: JWT-based authentication with role-based access

### API Endpoints

#### Authentication & Users
- `POST /users/login` - User authentication with JWT tokens
- `GET /admin/getWorker` - Worker management endpoints

#### Product Management
- `GET /admin/getProductsWithMachines` - Products with machine assignments
- `POST /admin/addMultiMachineProduct` - Create products for multiple machines
- `PUT /admin/updateProductMachineCompletion` - Update machine-specific completion

#### Shift Management
- `POST /admin/simplified-shifts/assign` - Assign workers to shifts
- `GET /admin/simplified-shifts/worker/{id}/week/{date}` - Weekly assignments
- `GET /admin/simplified-shifts/machine/{id}/week/{date}` - Machine assignments

#### Piece Analytics
- `POST /admin/uploadPieceExcel` - Excel upload with validation
- `GET /admin/downloadPieceExcel` - Export piece data
- `GET /admin/getPieceStatistics` - Date-wise analytics
- `GET /admin/downloadPieceTemplate` - Excel template download

#### Performance Analytics
- `GET /admin/getWorkerAnalytics` - Worker performance data
- `GET /admin/gettotalcost` - Cost calculations with date ranges

## Tech Stack

- **Spring Boot 3.5.7** - Main framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **Apache POI** - Excel file processing
- **JWT** - Token-based authentication
- **Maven** - Dependency management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Installation & Setup

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE fabric_management;

-- Create user (optional)
CREATE USER fabric_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE fabric_management TO fabric_user;
```

### 2. Application Configuration
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/fabric_management
spring.datasource.username=fabric_db
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. Build & Run
```bash
# Navigate to backend directory
cd fabric_backend

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Project Structure

```
src/main/java/com/example/fabric/
├── config/
│   ├── SecurityConfig.java         # Security configuration
│   └── CorsConfig.java            # CORS configuration
├── controller/
│   ├── UserController.java        # Authentication endpoints
│   ├── ProductController.java     # Product management
│   ├── PieceController.java       # Piece management with analytics
│   ├── SimplifiedShiftController.java # Shift assignments
│   ├── MeterController.java       # Production meters
│   ├── WorkerController.java      # Worker management
│   ├── MachineController.java     # Machine management
│   └── FileController.java        # File operations
├── dto/
│   ├── AddMultiMachineProductDto.java # Multi-machine product creation
│   ├── UpdateProductMachineCompletionDto.java # Completion updates
│   ├── WorkerShiftAssignmentDto.java # Shift assignment data
│   ├── ProductWithMachinesDto.java # Product with machine info
│   └── ExcelMeterUploadResult.java # Excel processing results
├── enums/
│   └── ShiftType.java             # MORNING/EVENING shift types
├── filter/
│   ├── JwtAuthFilter.java         # JWT authentication filter
│   └── CorsFilter.java            # CORS handling
├── model/
│   ├── User.java                  # User entity
│   ├── Worker.java                # Worker entity
│   ├── Machine.java               # Machine entity
│   ├── Product.java               # Product entity
│   ├── ProductMachine.java        # Product-machine junction
│   ├── Shift.java                 # Shift entity
│   ├── WorkerShiftAssignment.java # Shift assignments
│   ├── Piece.java                 # Piece entity
│   ├── Meter.java                 # Production meter entity
│   └── StoredFile.java            # File storage entity
├── repository/
│   ├── UserRepository.java        # User data access
│   ├── ProductRepository.java     # Product data access
│   ├── ProductMachineRepository.java # Junction table access
│   ├── WorkerShiftAssignmentRepository.java # Shift data access
│   ├── PieceRepository.java       # Piece data access
│   ├── MeterRepository.java       # Meter data access
│   └── [Other repositories...]
├── services/
│   ├── ProductMachineService.java # Multi-machine product logic
│   ├── SimplifiedShiftAssignmentService.java # Shift management
│   ├── PieceService.java          # Piece analytics
│   ├── ExcelService.java          # Excel processing
│   ├── MeterService.java          # Production calculations
│   ├── WorkerService.java         # Worker analytics
│   ├── FileStorageService.java    # File management
│   └── [Other services...]
├── util/
│   ├── JwtUtil.java               # JWT token utilities
│   └── ResponseUtil.java          # API response utilities
└── FabricApplication.java         # Main application class
```

## Key Components

### Multi-Machine Product System
- **ProductMachineService**: Handles product creation across multiple machines
- **Normalized Structure**: Separate `products` and `product_machines` tables
- **Smart Completion Logic**: Product completion based on all machine statuses

### Simplified Shift Assignment
- **SimplifiedShiftAssignmentService**: Streamlined shift management
- **Machine Integration**: Shifts assigned per machine, not globally
- **Flexible Scheduling**: Morning (7 days) and Evening (6 days) patterns

### Excel Processing
- **ExcelService**: Comprehensive Excel upload/download functionality
- **Validation**: Data validation with detailed error reporting
- **Template Generation**: Pre-formatted Excel templates

### Analytics Engine
- **PieceService**: Date-wise analytics and statistics
- **WorkerService**: Performance analytics with cost calculations
- **Chart Generation**: Server-side chart image generation

### Security Features
- **JWT Authentication**: Stateless token-based security
- **Role-based Access**: Admin and user role separation
- **CORS Configuration**: Proper cross-origin request handling

## Database Schema

### Core Tables
```sql
-- Products table (overall product info)
products (id, product_name, product_code, start_date, end_date, is_complete, ...)

-- Product-Machine junction table
product_machines (id, product_id, machine_id, is_complete, created_at, ...)

-- Simplified shift assignments
worker_shift_assignments (id, worker_id, machine_id, shift_type, assignment_date, ...)

-- Production meters
meters (id, worker_id, machine_id, product_id, production_date, meters, ...)

-- Pieces tracking
pieces (id, product_id, export_date, meters, created_at, ...)
```

### Key Relationships
- Products → ProductMachines (One-to-Many)
- Machines → ProductMachines (One-to-Many)
- Workers → WorkerShiftAssignments (One-to-Many)
- Machines → WorkerShiftAssignments (One-to-Many)

## API Documentation

### Authentication
```http
POST /users/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### Multi-Machine Product Creation
```http
POST /admin/addMultiMachineProduct
Content-Type: application/json

{
  "productName": "Product A",
  "productCode": "PA001",
  "machineIds": [1, 2, 3],
  "costout": 25.50,
  "pointDecrease": 2.5
}
```

### Shift Assignment
```http
POST /admin/simplified-shifts/assign
Content-Type: application/json

{
  "workerId": 1,
  "machineId": 1,
  "shiftType": "MORNING",
  "weekStartDate": "2026-01-04"
}
```

### Piece Analytics
```http
GET /admin/getPieceStatistics?productId=1&startDate=2026-01-01&endDate=2026-01-31
```

## Configuration Options

### JWT Settings
```properties
jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000  # 24 hours in milliseconds
```

### File Upload Settings
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.storage.path=./storage/files
```

### Database Settings
```properties
spring.jpa.hibernate.ddl-auto=update  # Use 'validate' in production
spring.jpa.show-sql=false  # Set to false in production
```

## Development Guidelines

### Code Organization
- Controllers handle HTTP requests and responses
- Services contain business logic
- Repositories handle data access
- DTOs for data transfer between layers
- Entities represent database tables

### Error Handling
- Global exception handling with `@ControllerAdvice`
- Consistent error response format
- Detailed validation error messages
- Proper HTTP status codes

### Security Best Practices
- JWT tokens with expiration
- Password encoding with BCrypt
- CORS configuration for frontend integration
- Input validation and sanitization

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### API Testing
Use tools like Postman or curl to test endpoints:
```bash
# Login
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Get products (with JWT token)
curl -X GET http://localhost:8080/admin/getProductsWithMachines \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Deployment

### Production Configuration
1. Update `application-prod.properties`
2. Set environment variables for sensitive data
3. Use external database configuration
4. Enable SSL/HTTPS
5. Configure proper logging levels

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/fabric-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Monitoring & Maintenance

### Health Checks
- Spring Boot Actuator endpoints
- Database connection monitoring
- File system space monitoring

### Logging
- Structured logging with appropriate levels
- Error tracking and alerting
- Performance monitoring

This backend application provides a robust, scalable foundation for fabric production management with modern Spring Boot practices and comprehensive feature coverage.