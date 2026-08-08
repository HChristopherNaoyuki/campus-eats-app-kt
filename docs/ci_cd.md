# CI/CD Configuration

This document outlines the Continuous Integration (CI) and Continuous Deployment (CD) strategy for
the Campus Eats project.

## Table of Contents

1. [Pipeline Purpose](#pipeline-purpose)
2. [Workflow: CI - Continuous Integration](#workflow-ci---continuous-integration)
3. [Workflow: Build and Release](#workflow-build-and-release)
4. [Caching Strategy](#caching-strategy)
5. [Artifact Management](#artifact-management)

---

## Pipeline Purpose

The primary goal of the CI/CD pipeline is to ensure that the codebase remains healthy, builds
successfully, and passes all validation checks before changes are merged or released.

---

## Workflow: CI - Continuous Integration

**Path**: `.github/workflows/ci.yml`

This workflow is triggered on every push or pull request to the `master` and `release` branches. It
is designed to be fast and provide immediate feedback to developers.

### Jobs and Steps

1. **Static Analysis (Lint)**: Runs the Android Gradle Lint tool to check for code smells, potential
   bugs, and styling issues.
2. **Compilation**: Verifies that the project compiles by assembling a debug build.
3. **Unit Testing**: Executes all JUnit tests to ensure business logic remains correct.

---

## Workflow: Build and Release

**Path**: `.github/workflows/build-release.yml`

This workflow is triggered on pushes to the `release` branch. It performs a full build and generates
production-ready artifacts.

### Key Outputs

- **Debug APK**: For internal testing.
- **Release APK**: Signed APK for distribution.
- **Android App Bundle (AAB)**: Optimized bundle for Google Play Store submission.

---

## Caching Strategy

To minimize execution time, the pipeline utilizes GitHub Actions' caching mechanism for Gradle
dependencies.

- **Key**:
  `gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}`
- **Restore Key**: `gradle-${{ runner.os }}-`

This ensures that repeated runs reuse the same library downloads unless the build configuration
changes.

---

## Artifact Management

Build reports and binary artifacts are uploaded to GitHub Actions as downloadable artifacts.

- **Reports**: Lint results and unit test summaries.
- **Binaries**: APK and AAB files located in the `app/build/outputs/` directory.

---

*END OF DOCUMENT*
