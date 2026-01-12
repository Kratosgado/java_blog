# Database Setup Guide

This project uses a **hybrid database architecture** with PostgreSQL (structured data) and MongoDB (unstructured data).

## Quick Start

### Initial Setup
```bash
# Complete setup (creates containers and seeds data)
./setup-databases.sh
```

## Database Credentials

### PostgreSQL
- **Host**: localhost:5432
- **Database**: blog_db
- **User**: blog_user
- **Password**: blog_password

### MongoDB
- **Host**: localhost:27017
- **Database**: blog_nosql

## Daily Development

```bash
# Start databases
./dev.sh start

# Check status
./dev.sh status

# Stop databases
./dev.sh stop
```
