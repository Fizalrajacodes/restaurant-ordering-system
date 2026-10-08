# Quick Start Guide

## Prerequisites Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] Maven 3.8+ installed (`mvn -version`)
- [ ] PostgreSQL 15+ installed and running
- [ ] Razorpay account created (for payments)

## 5-Minute Setup

### Step 1: Database Setup (1 minute)

```bash
# Create database
createdb restaurant_db

# Or using psql
psql -c "CREATE DATABASE restaurant_db;"
```

### Step 2: Configuration (1 minute)

```bash
# Copy environment file
cp .env.example .env

# Edit .env with your settings
notepad .env
```

**Minimum required changes in `.env`:**
```env
DATABASE_PASSWORD=your_postgres_password
RAZORPAY_KEY_ID=your_razorpay_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
```

### Step 3: Start Backend (2 minutes)

```bash
cd backend

# Build and run
mvn clean install spring-boot:run
```

Backend will start on `http://localhost:8080`

### Step 4: Start Admin Dashboard (1 minute)

Open a new terminal:

```bash
cd frontend/java

# Run JavaFX application
mvn javafx:run
```

### Step 5: Access Application

**Customer Menu:**
```
http://localhost:8080/menu?table=TABLE-01
```

**Admin Dashboard:**
- Email: `admin@restaurant.com`
- Password: `admin123`

## Testing Payment Flow

1. Open customer menu: `http://localhost:8080/menu?table=TABLE-01`
2. Add items to cart
3. Enter customer details
4. Proceed to payment
5. Use Razorpay Test Card:
   - Card: `4111 1111 1111 1111`
   - CVV: `123`
   - Expiry: Any future date
   - OTP: `123456` (if prompted)

## Generating QR Codes

### Via Admin Dashboard (JavaFX)
1. Login to admin dashboard
2. Go to "Tables" tab
3. Click "Generate All QR"
4. Or select individual table and click "Generate"

### Via API
```bash
# Generate QR for table 1
curl -X POST http://localhost:8080/api/admin/tables/1/qr \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Common Issues

### Port Already in Use
```bash
# Change port in application.yml
server:
  port: 8081
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT 1;"

# Verify credentials in .env
```

### Razorpay Payment Failed
```bash
# Verify keys in .env
# Check webhook URL is accessible
# Use test mode for development
```

## Next Steps

1. **Customize Menu**: Add your restaurant's menu items via admin dashboard
2. **Configure Tables**: Add all your restaurant tables
3. **Generate QR Codes**: Print and place on tables
4. **Test Order Flow**: Place test orders
5. **Go Live**: Switch to Razorpay live mode

## Production Deployment

See README.md for detailed production deployment instructions.

## Support

For issues or questions:
- Check README.md documentation
- Review logs in `logs/restaurant-system.log`
- Contact: support@restaurant.com