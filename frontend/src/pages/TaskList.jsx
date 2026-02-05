import React, { useState, useEffect } from 'react';
import {
  Paper, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Box, FormControl, InputLabel, Select, MenuItem
} from '@mui/material';
import axios from 'axios';

function TaskList() {
  const [tasks, setTasks] = useState([]);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetchTasks();
    const interval = setInterval(fetchTasks, 5000);
    return () => clearInterval(interval);
  }, [filter]);

  const fetchTasks = async () => {
    try {
      const params = filter !== 'ALL' ? `?status=${filter}` : '';
      const response = await axios.get(`/api/tasks${params}`);
      setTasks(response.data.content || []);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    }
  };

  const getStatusColor = (status) => {
    switch(status) {
      case 'COMPLETED': return 'success';
      case 'PROCESSING': return 'info';
      case 'FAILED': return 'error';
      case 'PENDING': return 'warning';
      default: return 'default';
    }
  };

  const getPriorityColor = (priority) => {
    switch(priority) {
      case 'HIGH': return '#f44336';
      case 'MEDIUM': return '#ff9800';
      case 'LOW': return '#4caf50';
      default: return '#9e9e9e';
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Task List
      </Typography>

      <Box sx={{ mb: 2 }}>
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Filter by Status</InputLabel>
          <Select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            label="Filter by Status"
          >
            <MenuItem value="ALL">All</MenuItem>
            <MenuItem value="PENDING">Pending</MenuItem>
            <MenuItem value="PROCESSING">Processing</MenuItem>
            <MenuItem value="COMPLETED">Completed</MenuItem>
            <MenuItem value="FAILED">Failed</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Task ID</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Priority</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Retries</TableCell>
              <TableCell>Created At</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tasks.map((task) => (
              <TableRow key={task.taskId}>
                <TableCell>{task.taskId.substring(0, 16)}...</TableCell>
                <TableCell>{task.taskType}</TableCell>
                <TableCell>
                  <Chip 
                    label={task.priority} 
                    size="small"
                    sx={{ backgroundColor: getPriorityColor(task.priority), color: 'white' }}
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={task.status} 
                    color={getStatusColor(task.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>{task.retryCount}/{task.maxRetries}</TableCell>
                <TableCell>
                  {new Date(task.createdAt).toLocaleString()}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {tasks.length === 0 && (
        <Typography sx={{ mt: 2, textAlign: 'center' }}>
          No tasks found
        </Typography>
      )}
    </Box>
  );
}

export default TaskList;
