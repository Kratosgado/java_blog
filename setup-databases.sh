#!/bin/sh

# ================================================================
# Smart Blogging Platform - Database Setup Script
# Sets up PostgreSQL and MongoDB with Docker and seeds data
# ================================================================

set -e # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Smart Blogging Platform Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ================================================================
# 1. Setup PostgreSQL
# ================================================================
echo -e "${YELLOW}[1/5] Setting up PostgreSQL...${NC}"

# Check if postgis container exists
if [ "$(docker ps -aq -f name=postgis)" ]; then
  echo -e "${YELLOW}PostgreSQL container exists. Stopping and removing...${NC}"
  docker stop postgis 2>/dev/null || true
  docker rm postgis 2>/dev/null || true
fi

# Create and start PostgreSQL container
echo -e "${GREEN}Creating PostgreSQL container...${NC}"
docker run -d \
  --name postgis \
  -e POSTGRES_DB=blog_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgis/postgis

# Wait for PostgreSQL to be ready
echo -e "${GREEN}Waiting for PostgreSQL to be ready...${NC}"
sleep 5

# Test connection
until docker exec postgis pg_isready -U postgres -d blog_db >/dev/null 2>&1; do
  echo -e "${YELLOW}Waiting for PostgreSQL...${NC}"
  sleep 2
done

echo -e "${GREEN}✓ PostgreSQL is ready!${NC}"
echo ""

# ================================================================
# 2. Setup MongoDB
# ================================================================
echo -e "${YELLOW}[2/5] Setting up MongoDB...${NC}"

# Check if mongodb container exists
if [ "$(docker ps -aq -f name=mongodb)" ]; then
  echo -e "${YELLOW}MongoDB container exists. Stopping and removing...${NC}"
  docker stop mongodb 2>/dev/null || true
  docker rm mongodb 2>/dev/null || true
fi

# Create and start MongoDB container
echo -e "${GREEN}Creating MongoDB container...${NC}"
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  mongo

# Wait for MongoDB to be ready
echo -e "${GREEN}Waiting for MongoDB to be ready...${NC}"
sleep 5

# Test MongoDB connection
until docker exec mongodb mongosh --eval "db.adminCommand('ping')" >/dev/null 2>&1; do
  echo -e "${YELLOW}Waiting for MongoDB...${NC}"
  sleep 2
done

echo -e "${GREEN}✓ MongoDB is ready!${NC}"
echo ""
# ================================================================
# Final Verification
# ================================================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Setup Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✓ PostgreSQL running on localhost:5432${NC}"
echo -e "  Database: blog_db"
echo -e "  User: postgres"
echo -e "  Password: posgres"
echo ""
echo -e "${GREEN}✓ MongoDB running on localhost:27017${NC}"
echo -e "  Database: blog_nosql"
echo -e "  Collections: comments, reviews"
echo ""
echo -e "${YELLOW}To stop databases:${NC}"
echo -e "  ./dev.sh exit"
echo ""
echo -e "${YELLOW}To start databases:${NC}"
echo -e "  ./dev.sh start"
echo ""
echo -e "${YELLOW}To connect to PostgreSQL:${NC}"
echo -e "  docker exec -it postgis psql -U postgres -d blog_db"
echo ""
echo -e "${YELLOW}To connect to MongoDB:${NC}"
echo -e "  docker exec -it mongodb mongosh blog_nosql"
echo ""
