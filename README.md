# Smart Restaurant QR Ordering & Payment System

A complete, production-ready restaurant QR code ordering system with payment integration, real-time order management, and admin dashboard.

## Features

### Customer Features
- QR Code Scanning - Auto table detection
- Digital Menu - Browse food items by category
- Search - Find dishes quickly
- Cart Management - Add/remove items, adjust quantities
- Secure Checkout - Razorpay payment integration
- Order Tracking - Real-time order status updates
- Order History - View past orders

### Admin Features
- Real-time Dashboard - Live order notifications
- Order Management - Accept, prepare, ready, serve orders
- Menu Management - Add/edit/delete menu items
- Category Management - Organize menu by categories
- Table Management - Manage restaurant tables
- QR Code Generation - Generate and print QR codes for tables
- Live Notifications - WebSocket-based real-time updates
- Reports & Analytics - Revenue, orders, popular items
- Staff Management - Admin, staff, kitchen roles
- Settings - Restaurant info, payment config

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.2
- Spring Web (REST APIs)
- Spring Data JPA / Hibernate
- Spring Security (JWT)
- Spring WebSocket / STOMP
- Spring Mail
- PostgreSQL 15+
- Flyway (Database Migrations)
- Maven

### Frontend (Customer)
- Thymeleaf Templates
- Bootstrap 5
- JavaScript (Vanilla)
- Axios (HTTP Client)
- Razorpay Checkout

### Frontend (Admin)
- JavaFX 21
- Apache HttpClient 5
- Jackson (JSON)
- WebSocket Client

### Payment
- Razorpay Integration
- Payment Verification
- Webhook Support

### QR Code
- ZXing Library

## Architecture

```
Customer QR Scan
      ↓
Restaurant Menu (Spring Boot + Thymeleaf)
      ↓
Cart & Checkout
      ↓
Razorpay Payment
      ↓
Order Creation (PostgreSQL)
      ↓
Payment Verification
      ↓
Order Confirmed
      ↓
Restaurant Dashboard (JavaFX)
      ↓
Real-time Updates (WebSocket)
```

## Project Structure

```
restaurant-ordering-system/
├── backend/
│   ├── src/main/java/com/restaurant/
│   │   ├── config/
│   │   ├── controller/
│   │   │   ├── customer/
│   │   │   └── admin/
│   │   ├── dtos/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   ├── util/
│   │   ├── websocket/
│   │   └── RestaurantApplication.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/migration/ (Flyway)
│       ├── static/ (CSS, JS, images)
│       └── templates/ (Thymeleaf)
├── frontend/java/ (JavaFX Admin Dashboard)
│   ├── src/main/java/com/restaurant/admin/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── service/
│   │   └── util/
│   └── src/main/resources/
│       ├── fxml/
│       ├── css/
│       └── images/
├── database/
│   ├── schema.sql
│   └── seed.sql
├── qr-codes/
├── docs/
├── .env.example
└── README.md
```

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Razorpay Account (for payments)
- Node.js 16+ (optional, for frontend tooling)

## Setup Instructions

### 1. Clone Repository

```bash
git clone <repository-url>
cd restaurant-ordering-system
```

### 2. Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE restaurant_db;
```

Or using command line:
```bash
createdb restaurant_db
```

### 3. Environment Configuration

Copy `.env.example` to `.env` and update values:

```bash
cp .env.example .env
```

Update the following in `.env`:
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USERNAME` - PostgreSQL username
- `DATABASE_PASSWORD` - PostgreSQL password
- `RAZORPAY_KEY_ID` - Your Razorpay Key ID
- `RAZORPAY_KEY_SECRET` - Your Razorpay Key Secret
- `RAZORPAY_WEBHOOK_SECRET` - Your Razorpay Webhook Secret
- `JWT_SECRET` - Strong random string for JWT signing

### 4. Build Backend

```bash
cd backend
mvn clean install
```

### 5. Run Backend

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 6. Run JavaFX Admin Dashboard

```bash
cd frontend/java
mvn clean javafx:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/restaurant-admin-fx-1.0.0.jar
```

### 7. Access Application

- Customer Menu: `http://localhost:8080/menu?table=TABLE-01`
- Admin Dashboard: Run JavaFX application and login with:
  - Email: `admin@restaurant.com`
  - Password: `admin123`

## Default Credentials

**Admin Account:**
- Email: `admin@restaurant.com`
- Password: `admin123`

**⚠️ IMPORTANT:** Change the default password after first login!

## Razorpay Configuration

### 1. Create Razorpay Account
Sign up at [https://razorpay.com](https://razorpay.com)

### 2. Get API Keys
- Go to Settings → API Keys
- Copy Key ID and Key Secret
- Add to `.env` file

### 3. Configure Webhook
- Go to Settings → Webhooks
- Add webhook URL: `https://your-domain.com/api/payments/webhook`
- Select events: `payment.captured`, `payment.failed`
- Copy Webhook Secret to `.env`

### 4. Test Mode
Use Razorpay Test Mode for development:
- Test Key ID: `rzp_test_...`
- Test cards: `4111 1111 1111 1111` (Visa)

## Database Schema

### Tables

1. **users** - Admin, staff, kitchen users
2. **restaurant_tables** - Restaurant tables
3. **categories** - Menu categories
4. **menu_items** - Food items
5. **orders** - Customer orders
6. **order_items** - Order line items
7. **payments** - Payment records
8. **order_status_history** - Order status audit trail
9. **qr_codes** - QR codes for tables

## API Documentation

### Customer APIs (No Authentication Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/menu/categories` | GET | Get all menu categories |
| `/api/menu/items` | GET | Get all available menu items |
| `/api/menu/categories/{id}/items` | GET | Get items by category |
| `/api/menu/items/search?q=` | GET | Search menu items |
| `/api/menu/table/{token}` | GET | Get table by QR token |
| `/api/orders` | POST | Create new order |
| `/api/orders/{orderNumber}` | GET | Get order details |
| `/api/orders/track/{orderNumber}?mobile=` | GET | Track order |
| `/api/payments/create` | POST | Create Razorpay payment |
| `/api/payments/verify` | POST | Verify payment |

### Admin APIs (JWT Authentication Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | Admin login |
| `/api/auth/refresh` | POST | Refresh token |
| `/api/auth/me` | GET | Get current user |
| `/api/admin/dashboard` | GET | Dashboard stats |
| `/api/admin/orders` | GET | List orders |
| `/api/admin/orders/{id}/status` | PUT | Update order status |
| `/api/admin/menu-items` | GET | List menu items |
| `/api/admin/menu-items` | POST | Create menu item |
| `/api/admin/menu-items/{id}` | PUT | Update menu item |
| `/api/admin/menu-items/{id}` | DELETE | Delete menu item |
| `/api/admin/categories` | GET | List categories |
| `/api/admin/categories` | POST | Create category |
| `/api/admin/tables` | GET | List tables |
| `/api/admin/tables` | POST | Create table |
| `/api/admin/tables/{id}/qr` | POST | Generate QR code |

## Order Status Flow

```
PENDING → CONFIRMED → PREPARING → READY → SERVED
                    ↓
                 CANCELLED
```

## Payment Flow

```
Customer clicks Pay
      ↓
Backend creates Razorpay Order
      ↓
Razorpay Order ID generated
      ↓
Customer opens Razorpay Checkout
      ↓
Customer completes payment
      ↓
Razorpay returns payment details
      ↓
Backend verifies payment signature
      ↓
Payment marked SUCCESS
      ↓
Restaurant order becomes CONFIRMED
```

## QR Code Flow

1. Admin creates table (e.g., TABLE-01)
2. Admin generates QR code for table
3. QR code contains URL: `http://localhost:8080/menu?table=TOKEN`
4. Customer scans QR
5. System validates token and shows menu
6. Table number is automatically identified

## Testing

### Run Backend Tests

```bash
cd backend
mvn test
```

### Test Coverage
- User authentication tests
- Menu API tests
- Table validation tests
- Order creation tests
- Payment creation tests
- Razorpay signature verification tests
- Order status update tests

## Production Deployment

### Backend Deployment

1. **Build JAR:**
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. **Run with Docker:**
   ```bash
   docker build -t restaurant-backend .
   docker run -p 8080:8080 --env-file .env restaurant-backend
   ```

3. **Or run directly:**
   ```bash
   java -jar target/restaurant-ordering-system-1.0.0.jar --spring.profiles.active=prod
   ```

### Database Migration

Flyway runs migrations automatically on startup. To run manually:

```bash
mvn flyway:migrate
```

### JavaFX Admin Deployment

Package as native executable:

```bash
cd frontend/java
mvn clean package
jpackage --input target --name RestaurantAdmin --main-jar restaurant-admin-fx-1.0.0.jar
```

### Environment Variables (Production)

```bash
# Database
DATABASE_URL=jdbc:postgresql://prod-db:5432/restaurant_db
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=secure_password

# Razorpay (Live Keys)
RAZORPAY_KEY_ID=rzp_live_...
RAZORPAY_KEY_SECRET=live_secret
RAZORPAY_WEBHOOK_SECRET=webhook_secret

# JWT (Use strong random key)
JWT_SECRET=production-jwt-secret-min-256-bits-random-string

# Server
SERVER_PORT=8080

# Restaurant
RESTAURANT_NAME=Your Restaurant
QR_BASE_URL=https://your-domain.com
```

## Security Features

- JWT Authentication for admin APIs
- BCrypt password hashing (cost factor 12)
- Role-based access control (ADMIN, STAFF, KITCHEN)
- Razorpay signature verification
- SQL injection protection via JPA
- CORS configuration
- Input validation
- Rate limiting (recommended for production)

## Performance Considerations

- HikariCP connection pooling (20 max connections)
- Lazy loading for JPA entities
- Database indexes on frequently queried fields
- Flyway for efficient migrations
- WebSocket for real-time updates
- Static resource caching

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running
- Verify database credentials in `.env`
- Ensure port 8080 is not in use

### QR codes not working
- Verify QR_BASE_URL is correct
- Check QR token exists in database
- Ensure table is active

### Payments not working
- Verify Razorpay keys in `.env`
- Check webhook URL is accessible
- Ensure payment URLs match domain

### WebSocket not connecting
- Check backend is running on port 8080
- Verify WebSocket endpoint `/ws` is accessible
- Check firewall/proxy settings

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

Proprietary - All rights reserved

## Support

For issues and questions:
- Create an issue in the repository
- Email: support@restaurant.com

## Changelog

### Version 1.0.0
- Initial release
- QR code ordering system
- Razorpay payment integration
- JavaFX admin dashboard
- Real-time WebSocket notifications
- Menu and table management
- Order tracking
- Reports and analytics