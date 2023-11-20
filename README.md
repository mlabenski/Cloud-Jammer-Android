# Cloud Jammer

Cloud Jammer is an immersive Android application designed for vape shop enthusiasts. It offers a sleek interface to browse and manage an extensive collection of vape flavors sorted by categories.

## Features

- **Browse Products**: Users can effortlessly navigate through a variety of products.
- **Brand Categorization**: Products are neatly organized under brand names for easy access.
- **Settings Configuration**: Store owners can configure the app with their specific store ID to tailor the product list.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

What things you need to install the software:

- Android Studio (Arctic Fox or later)
- Gradle Build System

### Installing

A step-by-step series of examples that tell you how to get a development environment running:

1. Clone the repo:
   ```sh
   git clone https://github.com/yourusername/cloud-jammer-york.git
Open the project in Android Studio.
Sync the project with Gradle files.
Run the project on an emulator or real device.

Usage
Provide examples on how to use the app with code snippets and screenshots. For instance:

To change the store ID:

kotlin
Copy code
val settingsIntent = Intent(context, SettingsActivity::class.java)
startActivity(settingsIntent)
