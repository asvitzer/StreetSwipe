# StreetSwipe

**StreetSwipe** is the easiest way for street performers to accept tips on the go. This sample Android app integrates with the Stripe Terminal SDK to process in-person payments using Tap to Pay. It includes the creation of payment intents, capturing payments, and simulating real-world transaction flows.

## Architecture

The app is built using the **MVVM** (Model-View-ViewModel) architecture pattern to maintain a clean separation of concerns and ensure testability and scalability. Here’s an overview of the key architectural components and dependencies used in the project:

### Dependency Categories

- **Dependency Injection**:
  - **Hilt**: Used for dependency injection to manage the app's components and provide a clean architecture with minimal boilerplate.
  - Libraries:
    - `hilt-android`
    - `hilt-android-compiler`
    - `androidx-hilt-navigation-compose`
  
- **Navigation**:
  - **Jetpack Navigation Compose**: Provides type-safe navigation between composable screens.
  - Libraries:
    - `androidx.navigation.compose`
    - `androidx.hilt.navigation.compose`
  
- **Asynchronous Programming**:
  - **Kotlin Coroutines**: Simplifies asynchronous programming, allowing for easy management of background tasks.
  - Libraries:
    - `kotlinx.coroutines.android`
  
- **Lifecycle Management**:
  - **ViewModel & LiveData**: Helps manage UI-related data in a lifecycle-conscious way, preventing memory leaks.
  - Libraries:
    - `androidx.lifecycle.viewmodel.ktx`
    - `androidx.lifecycle.runtime.ktx`
  
- **Networking**:
  - **Retrofit & OkHttp**: Used for network communication, providing a simple API for HTTP calls and handling JSON conversion.
  - Libraries:
    - `okhttp`
    - `retrofit`
    - `converter.gson`
  
- **Stripe Terminal SDK**:
  - Provides integration for in-person payments and supports features like payment intents and capturing payments.
  - Libraries:
    - `stripeterminal.localmobile`
    - `stripeterminal.core`

- **UI & Testing**:
  - **Jetpack Compose**: Modern toolkit for building native UI with Kotlin.
  - **Jetpack Compose UI Tests**: Provides tools for testing Jetpack Compose UIs to ensure the app's UI behaves as expected.
  - Libraries:
    - `androidx.ui`
    - `androidx.ui.tooling`
    - `androidx.ui.tooling.preview`
    - `androidx.ui.test.junit4`
    - `androidx.ui.test.manifest`

## Setup Instructions

Follow these steps to get StreetSwipe up and running:

1. **Set up the Backend**: 
   - You'll need a backend to handle connection tokens and payment capture. Use Stripe's example backend as a starting point.
   - See the setup instructions here: [Stripe Example Terminal Backend](https://github.com/stripe/example-terminal-backend)

2. **Create a Location for the Reader**:
   - In the Stripe Dashboard, create a location for your reader to enable in-person payments.
   - Create the location here: [Stripe Dashboard - Terminal Locations](https://dashboard.stripe.com/test/terminal/locations)

3. **Insert Your Backend URL**:
   - Add your backend URL in the project's `gradle.properties` file.
   - Example:
     ```properties
     EXAMPLE_BACKEND_URL="https://your-backend-url.com"
     ```

## Features

- **In-Person Payments**: Supports Tap to Pay functionality using the Stripe Terminal SDK.
- **MVVM Architecture**: Clean, maintainable, and testable codebase using the MVVM pattern.
- **Dependency Injection with Hilt**: Efficient and boilerplate-free dependency management.
- **Coroutines and LiveData**: Modern asynchronous programming with lifecycle-aware components.