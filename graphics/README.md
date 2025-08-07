# WSO2 API Manager Custom Lifecycle Graphics

This folder contains custom lifecycle visualization components for the WSO2 API Manager Publisher Portal.

## Contents

- `LifeCycleImage.jsx` - React component that renders an interactive SVG lifecycle diagram showing API states and transitions

## Installation Instructions

### Prerequisites

- WSO2 API Manager 4.x or later
- Node.js and npm for building React components
- Access to the WSO2 APIM Publisher Portal source code
- **Important**: Review the official WSO2 documentation for advanced UI customization: https://apim.docs.wso2.com/en/latest/reference/customize-product/customizations/advanced-ui-customization/

### Installation Steps

1. **Locate the Publisher Portal source**:
   ```bash
   # In your WSO2 installation
   cd <APIM_HOME>/repository/deployment/server/webapps/publisher/
   ```

2. **Backup original files** (IMPORTANT):
   ```bash
   # Create backup directory
   mkdir -p backups/$(date +%Y%m%d_%H%M%S)
   
   # Backup existing lifecycle components (if they exist)
   cp -r <publisher-source>/src/main/webapp/source/src/app/components/Apis/Details/LifeCycle/ \
         backups/$(date +%Y%m%d_%H%M%S)/original-lifecycle/
   
   # Backup the entire publisher webapp (recommended)
   tar -czf backups/publisher-webapp-backup-$(date +%Y%m%d_%H%M%S).tar.gz \
       <APIM_HOME>/repository/deployment/server/webapps/publisher/
   ```

3. **Copy the JSX component**:
   ```bash
   # Copy to the appropriate React components directory
   cp LifeCycleImage.jsx <publisher-source>/src/main/webapp/source/src/app/components/Apis/Details/LifeCycle/
   ```

3. **Install dependencies** (if not already present):
   ```bash
   # In the publisher source directory
   npm install @mui/material prop-types react-intl
   ```

## Compilation Instructions

### For Development

1. **Navigate to publisher source**:
   ```bash
   cd <APIM_HOME>/repository/deployment/server/webapps/publisher/
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Build for development**:
   ```bash
   npm run build:dev
   ```

### For Production

1. **Build optimized version**:
   ```bash
   npm run build:prod
   ```

2. **Deploy to WSO2 APIM**:
   ```bash
   # Copy built files to the webapps directory
   cp -r dist/* <APIM_HOME>/repository/deployment/server/webapps/publisher/
   ```

## Component Features

- **Interactive Lifecycle States**: Created, Pre-Released, Published, Promoted, Deprecated, Blocked, Retired
- **Visual State Highlighting**: Current state is highlighted with full opacity, others dimmed
- **Transition Arrows**: Visual representation of allowed state transitions
- **Internationalization**: Supports i18n through React Intl
- **Material-UI Styling**: Uses MUI styled components for consistent theming

## State Transitions Supported

The component visualizes these lifecycle transitions:
- Created → Pre-Released → Published → Promoted → Deprecated → Retired
- Various reverse transitions and direct paths between states
- Block/Unblock functionality from Published state

## Rollback Instructions

If you need to restore the original files:

```bash
# Stop WSO2 APIM
<APIM_HOME>/bin/wso2server.sh stop

# Restore from backup
tar -xzf backups/publisher-webapp-backup-YYYYMMDD_HHMMSS.tar.gz -C /

# Or restore just the lifecycle components
cp -r backups/YYYYMMDD_HHMMSS/original-lifecycle/* \
      <publisher-source>/src/main/webapp/source/src/app/components/Apis/Details/LifeCycle/

# Restart WSO2 APIM
<APIM_HOME>/bin/wso2server.sh start
```

## Troubleshooting

- **Always backup before making changes** - this prevents data loss
- Ensure all dependencies are installed before building
- Verify file permissions in the WSO2 deployment directory
- Check browser console for JavaScript errors after deployment
- Confirm that the lifecycle configuration matches the component's expected states
- If deployment fails, restore from backup and investigate the issue