#!/bin/bash

# Generate coverage report for blog-backend
echo "🔍 Running tests and generating coverage report..."
mvn clean test

if [ -f target/site/jacoco/index.html ]; then
    echo ""
    echo "✅ Coverage report generated successfully!"
    echo ""
    echo "📊 Report location:"
    echo "   file://$(pwd)/target/site/jacoco/index.html"
    echo ""
    echo "📈 Quick stats:"
    grep -A 1 "Total" target/site/jacoco/index.html | grep -oP '\d+%' | head -2 | paste -sd ' ' | awk '{print "   Instruction Coverage: " $1 "\n   Branch Coverage: " $2}'
    echo ""
    
    # Try to open in default browser
    if command -v xdg-open &> /dev/null; then
        echo "🌐 Opening report in browser..."
        xdg-open target/site/jacoco/index.html
    elif command -v open &> /dev/null; then
        echo "🌐 Opening report in browser..."
        open target/site/jacoco/index.html
    else
        echo "💡 Open the file above in your browser to view the report"
    fi
else
    echo "❌ Failed to generate coverage report"
    exit 1
fi
