import React, { useState, useEffect } from 'react';
import { Grid, Paper, Typography, Box } from '@mui/material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';

function MetricCard({ title, value, color = '#1976d2' }) {
  return (
    <Paper sx={{ p: 3, display: 'flex', flexDirection: 'column', height: 140 }}>
      <Typography component="h2" variant="h6" color="primary" gutterBottom>
        {title}
      </Typography>
      <Typography component="p" variant="h4" sx={{ color }}>
        {value}
      </Typography>
    </Paper>
  );
}

function Dashboard() {
  const [metrics, setMetrics] = useState({
    totalTasks: 0,
    completedTasks: 0,
    failedTasks: 0,
    pendingTasks: 0,
    processingTasks: 0,
    avgProcessingTime: 0,
    queueSizes: { high: 0, medium: 0, low: 0 },
    successRate: 0
  });

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const response = await axios.get('/api/metrics');
        setMetrics(response.data);
      } catch (error) {
        console.error('Error fetching metrics:', error);
      }
    };

    fetchMetrics();
    const interval = setInterval(fetchMetrics, 5000);
    return () => clearInterval(interval);
  }, []);

  const queueData = [
    { name: 'High', tasks: metrics.queueSizes.high },
    { name: 'Medium', tasks: metrics.queueSizes.medium },
    { name: 'Low', tasks: metrics.queueSizes.low }
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard title="Total Tasks" value={metrics.totalTasks} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard title="Completed" value={metrics.completedTasks} color="#4caf50" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard title="Failed" value={metrics.failedTasks} color="#f44336" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard title="Pending" value={metrics.pendingTasks} color="#ff9800" />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Queue Sizes
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={queueData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="tasks" stroke="#8884d8" />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              System Stats
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography>Success Rate: {metrics.successRate}%</Typography>
              <Typography>Avg Processing Time: {Math.round(metrics.avgProcessingTime)}ms</Typography>
              <Typography>Processing: {metrics.processingTasks}</Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

export default Dashboard;
