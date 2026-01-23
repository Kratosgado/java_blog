#!/bin/bash

# ================================================================
# Smart Blogging Platform - Development Script
# Manage PostgreSQL/MongoDB Docker and Spring Boot/JavaFX apps
# ================================================================

case "$1" in
start)
  echo "Starting databases..."
  docker start postgis mongodb
  echo "✓ Databases started!"
  echo ""
  echo "PostgreSQL: localhost:5432 (blog_db)"
  echo "MongoDB:    localhost:27017 (blog_nosql)"
  ;;
run)
  if [ "$2" == "all" ]; then
    echo "Running all applications..."
    cd blog-backend && mvn clean spring-boot:run &
    cd blog-frontend && mvn clean javafx:run
  elif [ "$2" == "backend" ] || [ "$2" == "api" ]; then
    echo "Running Spring Boot backend..."
    cd blog-backend && mvn clean spring-boot:run
  elif [ "$2" == "frontend" ] || [ "$2" == "ui" ]; then
    echo "Running JavaFX frontend..."
    cd blog-frontend && mvn clean javafx:run
  else
    echo "Usage: $0 run {backend|frontend}"
    echo "  backend  - Run Spring Boot REST API (port 8080)"
    echo "  frontend - Run JavaFX desktop application"
  fi
  ;;
build)
  echo "Building all modules..."
  mvn clean install -DskipTests
  ;;
stop | exit)
  echo "Stopping databases..."
  docker stop postgis mongodb
  echo "✓ Databases stopped!"
  ;;
setup)
  echo "Running complete database setup..."
  ./setup-databases.sh
  ;;
test)
  echo "Running all tests..."
  cd blog-backend && mvn clean test
  ;;
reset)
  echo "⚠️  This will DELETE all data and recreate databases!"
  read -p "Are you sure? (y/N): " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./setup-databases.sh
  else
    echo "Reset cancelled."
  fi
  ;;
logs)
  if [ "$2" == "postgres" ] || [ "$2" == "postgresql" ] || [ "$2" == "pg" ]; then
    docker logs -f postgis
  elif [ "$2" == "mongo" ] || [ "$2" == "mongodb" ]; then
    docker logs -f mongodb
  else
    echo "Usage: $0 logs {postgres|mongo}"
    echo "Show logs for specific database"
  fi
  ;;
shell)
  if [ "$2" == "postgres" ] || [ "$2" == "postgresql" ] || [ "$2" == "pg" ]; then
    docker exec -it postgis psql -U blog_user -d blog_db
  elif [ "$2" == "mongo" ] || [ "$2" == "mongodb" ]; then
    docker exec -it mongodb mongosh blog_nosql
  else
    echo "Usage: $0 shell {postgres|mongo}"
    echo "Open database shell"
  fi
  ;;
status)
  echo "Status:"
  echo "================"
  echo ""
  if [ "$(docker ps -q -f name=postgis)" ]; then
    echo "✓ PostgreSQL: Running"
  else
    echo "✗ PostgreSQL: Stopped"
  fi
  if [ "$(docker ps -q -f name=mongodb)" ]; then
    echo "✓ MongoDB: Running"
  else
    echo "✗ MongoDB: Stopped"
  fi
  ;;
*)
  echo "Smart Blogging Platform - Development Script"
  echo ""
  echo "Usage: $0 {command} [options]"
  echo ""
  echo "Commands:"
  echo "  build   - Build all Maven modules"
  echo "  run     - Run application (backend|frontend)"
  echo "  setup   - Complete database setup (creates containers and seeds data)"
  echo "  start   - Start existing database containers"
  echo "  stop    - Stop database containers"
  echo "  reset   - Delete and recreate databases (CAUTION: destroys data)"
  echo "  status  - Check database container status"
  echo "  logs    - View database logs (postgres|mongo)"
  echo "  shell   - Open database shell (postgres|mongo)"
  echo ""
  echo "Examples:"
  echo "  $0 build              # Build all modules"
  echo "  $0 setup              # First time setup"
  echo "  $0 start              # Start databases"
  echo "  $0 run backend        # Run Spring Boot API"
  echo "  $0 run frontend       # Run JavaFX app"
  echo "  $0 logs postgres      # View PostgreSQL logs"
  echo "  $0 shell mongo        # Open MongoDB shell"
  exit 1
  ;;
esac
