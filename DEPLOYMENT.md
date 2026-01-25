# DEPLOYMENT - Production Deployment Guide

## 🚀 Deployment Environments

### Supported Platforms

| Platform | Cost | Features | Best For |
|----------|------|----------|----------|
| **Render** | Free-$100+/mo | Easy setup, auto-deploy, PostgreSQL | MVP, Startups |
| **Heroku** | Free-$500+/mo | Platform-as-a-Service, add-ons | Rapid deployment |
| **AWS EC2** | $5-200+/mo | Full control, scalable | Enterprise |
| **Docker** | Custom | Containerized, portable | Microservices |
| **DigitalOcean** | $5-100+/mo | Simple VPS, good pricing | Small-medium apps |

---

## 🎯 Render Deployment (Recommended for Beginners)

### Prerequisites

- Render account (https://render.com)
- GitHub repository
- Application code pushed to GitHub
- Environment variables ready

### Step 1: Connect GitHub Repository

1. Go to https://dashboard.render.com
2. Click "New +" → "Web Service"
3. Select "GitHub" as repository source
4. Authorize Render to access your GitHub
5. Select repository and branch to deploy

### Step 2: Configure Build & Deploy

```
Name: ecommerce-backend
Environment: Docker
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/ecommerce-backend-*.jar
Region: Singapore (or closest to your users)
Instance Type: Starter ($7/month)
```

### Step 3: Set Environment Variables

In Render Dashboard → "Environment" tab:

```env
# ======================= ADMIN CONFIGURATION =======================
# Bootstrap admin user on application startup
# These credentials are used to create the initial admin account
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_PASSWORD=<strong-password-32-chars-min>
ADMIN_FULL_NAME=Production Administrator

# Database (Render PostgreSQL)
DATABASE_URL=postgresql://user:password@host:5432/database

# Or External MySQL
DATABASE_URL=jdbc:mysql://your-mysql-host:3306/database
DB_USERNAME=username
DB_PASSWORD=password

# JWT Secrets
JWT_ACCESS_SECRET=<32+ char strong secret>
JWT_REFRESH_SECRET=<32+ char strong secret>
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# OAuth2
GOOGLE_CLIENT_ID=<your-client-id>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<your-secret>
GOOGLE_REDIRECT_URI=https://your-render-app.onrender.com/api/v1/auth/oauth2/callback

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=your-app-email@gmail.com
MAIL_PASSWORD=<app-specific-password>
SHIPPING_TEAM_EMAIL=shipping@yourdomain.com

# External Services
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-key>
CLOUDINARY_API_SECRET=<your-secret>

RAZORPAY_KEY_ID=<your-key-id>
RAZORPAY_KEY_SECRET=<your-secret>

# Database Connection Pool
DB_POOL_SIZE=5
DB_MIN_IDLE=2
DB_DIALECT=org.hibernate.dialect.PostgreSQL10Dialect
```

### Step 4: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
```

### Step 5: Deploy

```bash
# Push to GitHub
git push origin main

# Render auto-detects and starts deployment
# Check deployment status in Render Dashboard
```

### Step 6: Verify Deployment

```bash
# Health check
curl https://your-app-name.onrender.com/actuator/health

# Swagger API docs
https://your-app-name.onrender.com/swagger-ui.html

# API test
curl https://your-app-name.onrender.com/api/v1/products
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build image
docker build -t ecommerce-backend:1.0 .

# Tag for registry
docker tag ecommerce-backend:1.0 myregistry/ecommerce-backend:1.0

# Push to registry
docker push myregistry/ecommerce-backend:1.0
```

### Run Docker Container

**Development:**
```bash
docker run -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_NAME=ecommerce_backend_db \
  -e DB_USER=ecom_user \
  -e DB_PASSWORD=password \
  -e JWT_ACCESS_SECRET=<secret> \
  -p 8080:8080 \
  ecommerce-backend:1.0
```

**Production:**
```bash
docker run -d \
  --name ecommerce-backend \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:mysql://prod-mysql:3306/ecommerce \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=<secure-password> \
  -e JWT_ACCESS_SECRET=<32-char-secret> \
  -e JWT_REFRESH_SECRET=<32-char-secret> \
  -e GOOGLE_CLIENT_ID=<client-id> \
  -e GOOGLE_CLIENT_SECRET=<secret> \
  -e MAIL_USER=<email> \
  -e MAIL_PASSWORD=<password> \
  -p 8080:8080 \
  -v logs:/app/logs \
  myregistry/ecommerce-backend:1.0
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: ecommerce_backend_db
      MYSQL_USER: ecom_user
      MYSQL_PASSWORD: ecom_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:mysql://mysql:3306/ecommerce_backend_db
      DB_USERNAME: ecom_user
      DB_PASSWORD: ecom_password
      JWT_ACCESS_SECRET: ${JWT_ACCESS_SECRET}
      # ... other env vars
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs

volumes:
  mysql_data:
```

Deploy with:
```bash
docker-compose up -d
```

---

## ☁️ AWS Deployment (EC2 + RDS)

### RDS Setup

```bash
# Create RDS MySQL instance
aws rds create-db-instance \
  --db-instance-identifier ecommerce-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password <secure-password> \
  --allocated-storage 20 \
  --publicly-accessible false
```

### EC2 Setup

```bash
# Launch EC2 instance
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.small \
  --key-name my-key-pair \
  --security-groups default

# SSH into instance
ssh -i my-key-pair.pem ec2-user@<public-ip>

# Install Java 21
sudo yum update -y
sudo yum install -y java-21-amazon-corretto

# Clone repository
git clone https://github.com/yourusername/ecommerce-backend.git
cd ecommerce-backend

# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:mysql://rds-endpoint:3306/ecommerce_backend_db
# ... set other variables

# Build and run
mvn clean package
java -jar target/ecommerce-backend-*.jar
```

---

## 🔄 Kubernetes Deployment

### Deployment Manifest

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ecommerce-backend
  labels:
    app: ecommerce-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ecommerce-backend
  template:
    metadata:
      labels:
        app: ecommerce-backend
    spec:
      containers:
      - name: ecommerce-backend
        image: myregistry/ecommerce-backend:1.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-url
        - name: JWT_ACCESS_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: jwt-access-secret
        # ... other env vars
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 500m
            memory: 1Gi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ecommerce-backend-service
spec:
  selector:
    app: ecommerce-backend
  type: LoadBalancer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
```

Deploy:
```bash
kubectl apply -f k8s/deployment.yaml
```

---

## 📋 Pre-Deployment Checklist

- [ ] All tests passing (`mvn test`)
- [ ] No hardcoded secrets in code
- [ ] Environment variables configured
- [ ] Database migrations tested
- [ ] SSL/TLS certificate ready (if custom domain)
- [ ] Backup strategy defined
- [ ] Monitoring configured
- [ ] Error handling verified
- [ ] Rate limiting enabled
- [ ] Security headers configured
- [ ] CORS configured for production
- [ ] Logging configured appropriately
- [ ] Health check endpoints verified
- [ ] Load balancer configured (if applicable)
- [ ] CDN configured (if applicable)

---

## 🔒 SSL/HTTPS Setup

### Render (Automatic)

Render automatically provides free SSL certificates. Just configure custom domain:
1. Dashboard → Web Service → Custom Domains
2. Add your domain
3. Update DNS records per instructions
4. Certificate auto-renewed

### AWS (ACM + ALB)

```bash
# Request certificate
aws acm request-certificate \
  --domain-name yourdomain.com \
  --validation-method DNS

# Create Application Load Balancer
aws elbv2 create-load-balancer \
  --name ecommerce-alb \
  --subnets subnet-xxx subnet-yyy

# Add HTTPS listener
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:... \
  --protocol HTTPS \
  --port 443 \
  --certificates CertificateArn=arn:aws:acm:...
```

---

## 📊 Monitoring & Logging

### CloudWatch (AWS)

```bash
# Push logs to CloudWatch
aws logs create-log-group --log-group-name /aws/ecommerce-backend
aws logs put-retention-policy \
  --log-group-name /aws/ecommerce-backend \
  --retention-in-days 30
```

### Datadog

Add Datadog agent:
```yaml
dd:
  site: datadoghq.com
  api_key: ${DATADOG_API_KEY}
  
springdoc:
  monitoring:
    enabled: true
```

### ELK Stack (Elasticsearch, Logstash, Kibana)

Configure Logback:
```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Render

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: 21
      
      - name: Run tests
        run: mvn test
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Deploy to Render
        run: |
          curl -X POST https://api.render.com/deploy/srv-xxx \
            -H "Authorization: Bearer ${{ secrets.RENDER_API_KEY }}"
```

---

## 📦 Backup & Recovery

### Database Backup (MySQL)

```bash
# Automated daily backup
0 2 * * * mysqldump -u root -p ecommerce_backend_db > /backups/db-$(date +\%Y\%m\%d).sql

# Restore from backup
mysql -u root -p ecommerce_backend_db < /backups/db-20240125.sql
```

### AWS RDS Automated Backups

```bash
# Create snapshot
aws rds create-db-snapshot \
  --db-instance-identifier ecommerce-db \
  --db-snapshot-identifier ecommerce-db-backup-20240125

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier ecommerce-db-restored \
  --db-snapshot-identifier ecommerce-db-backup-20240125
```

---

## 🚀 Scaling Strategies

### Horizontal Scaling (Multiple Instances)

```bash
# Behind load balancer
LoadBalancer
├── Instance 1 (Port 8080)
├── Instance 2 (Port 8080)
└── Instance 3 (Port 8080)
```

### Vertical Scaling (Larger Instances)

```bash
# Upgrade instance type (e.g., t3.small → t3.large)
# More CPU, RAM, Network
```

### Database Optimization

```sql
-- Add indexes on frequently accessed columns
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_product_category_id ON products(category_id);

-- Use read replicas for read-heavy operations
-- Implement connection pooling
-- Archive old data
```

---

## 🚨 Rollback Procedure

### Version Control

```bash
# Tag releases
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Rollback to previous version
git checkout v1.0.0
mvn clean package
java -jar target/ecommerce-backend-1.0.0.jar
```

### Render Rollback

1. Go to Render Dashboard
2. Deployments tab
3. Select previous successful deployment
4. Click "Redeploy"

### Database Rollback

```bash
# Restore database from backup
mysql -u root -p ecommerce_backend_db < /backups/pre-migration.sql
```

---

## 📞 Support & Troubleshooting

### Common Deployment Issues

**App won't start:**
```bash
# Check logs
docker logs ecommerce-backend

# Verify environment variables
env | grep JWT_ACCESS_SECRET

# Check database connection
mysql -h <host> -u <user> -p <database>
```

**Slow performance:**
```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics

# Check database query performance
EXPLAIN SELECT * FROM products WHERE category_id = 1;

# Monitor system resources
top
free -h
```

---

**For Render Deployment:** See [.env.render.example](.env.render.example)

**For Local Testing Before Deployment:** See [README.md](README.md#quick-start)

**For Security:** See [SECURITY.md](SECURITY.md)

**For Architecture:** See [ARCHITECTURE.md](ARCHITECTURE.md)
