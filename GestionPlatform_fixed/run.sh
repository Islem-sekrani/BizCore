#!/bin/bash

echo "========================================"
echo "  Admin Panel - Gestion Platform"
echo "========================================"
echo ""

echo "Compilation du projet avec Maven..."
mvn clean compile

echo ""
echo "Lancement de l'application..."
mvn javafx:run
