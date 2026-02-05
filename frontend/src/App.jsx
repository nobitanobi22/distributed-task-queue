import React, { useState } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box, AppBar, Toolbar, Typography, Container, Tabs, Tab } from '@mui/material';
import Dashboard from './pages/Dashboard';
import TaskSubmit from './pages/TaskSubmit';
import TaskList from './pages/TaskList';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  const renderContent = () => {
    switch(currentTab) {
      case 0:
        return <Dashboard />;
      case 1:
        return <TaskSubmit />;
      case 2:
        return <TaskList />;
      default:
        return <Dashboard />;
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ flexGrow: 1 }}>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              Distributed Task Queue System
            </Typography>
          </Toolbar>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            textColor="inherit"
            indicatorColor="secondary"
            sx={{ backgroundColor: '#1565c0' }}
          >
            <Tab label="Dashboard" />
            <Tab label="Submit Task" />
            <Tab label="Task List" />
          </Tabs>
        </AppBar>
        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
          {renderContent()}
        </Container>
      </Box>
    </ThemeProvider>
  );
}

export default App;
