import React, { useState, useEffect } from 'react';
import { 
  Paper, Typography, TextField, Select, MenuItem, Button, 
  FormControl, InputLabel, Box, Alert 
} from '@mui/material';
import axios from 'axios';

function TaskSubmit() {
  const [taskTypes, setTaskTypes] = useState([]);
  const [formData, setFormData] = useState({
    taskType: 'EMAIL_SEND',
    priority: 'MEDIUM',
    payload: {}
  });
  const [payloadFields, setPayloadFields] = useState({});
  const [result, setResult] = useState(null);

  useEffect(() => {
    fetchTaskTypes();
  }, []);

  const fetchTaskTypes = async () => {
    try {
      const response = await axios.get('/api/metrics/task-types');
      setTaskTypes(response.data);
      if (response.data.length > 0) {
        setFormData(prev => ({ ...prev, taskType: response.data[0] }));
      }
    } catch (error) {
      console.error('Error fetching task types:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('/api/tasks/submit', {
        taskType: formData.taskType,
        priority: formData.priority,
        payload: payloadFields,
        maxRetries: 3
      });
      setResult({ type: 'success', data: response.data });
    } catch (error) {
      setResult({ type: 'error', message: error.message });
    }
  };

  const renderPayloadFields = () => {
    switch(formData.taskType) {
      case 'EMAIL_SEND':
        return (
          <>
            <TextField
              fullWidth
              label="To Email"
              value={payloadFields.to || ''}
              onChange={(e) => setPayloadFields({...payloadFields, to: e.target.value})}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Subject"
              value={payloadFields.subject || ''}
              onChange={(e) => setPayloadFields({...payloadFields, subject: e.target.value})}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Body"
              multiline
              rows={4}
              value={payloadFields.body || ''}
              onChange={(e) => setPayloadFields({...payloadFields, body: e.target.value})}
              margin="normal"
            />
          </>
        );
      case 'IMAGE_PROCESS':
        return (
          <>
            <TextField
              fullWidth
              label="Image URL"
              value={payloadFields.imageUrl || ''}
              onChange={(e) => setPayloadFields({...payloadFields, imageUrl: e.target.value})}
              margin="normal"
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Operation</InputLabel>
              <Select
                value={payloadFields.operation || 'resize'}
                onChange={(e) => setPayloadFields({...payloadFields, operation: e.target.value})}
              >
                <MenuItem value="resize">Resize</MenuItem>
                <MenuItem value="compress">Compress</MenuItem>
                <MenuItem value="thumbnail">Thumbnail</MenuItem>
              </Select>
            </FormControl>
          </>
        );
      case 'REPORT_GENERATE':
        return (
          <>
            <FormControl fullWidth margin="normal">
              <InputLabel>Report Type</InputLabel>
              <Select
                value={payloadFields.reportType || 'sales'}
                onChange={(e) => setPayloadFields({...payloadFields, reportType: e.target.value})}
              >
                <MenuItem value="sales">Sales</MenuItem>
                <MenuItem value="analytics">Analytics</MenuItem>
                <MenuItem value="user-activity">User Activity</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Date Range"
              value={payloadFields.dateRange || ''}
              onChange={(e) => setPayloadFields({...payloadFields, dateRange: e.target.value})}
              margin="normal"
            />
          </>
        );
      default:
        return null;
    }
  };

  return (
    <Paper sx={{ p: 3, maxWidth: 600, mx: 'auto' }}>
      <Typography variant="h5" gutterBottom>
        Submit New Task
      </Typography>
      
      <form onSubmit={handleSubmit}>
        <FormControl fullWidth margin="normal">
          <InputLabel>Task Type</InputLabel>
          <Select
            value={formData.taskType}
            onChange={(e) => {
              setFormData({...formData, taskType: e.target.value});
              setPayloadFields({});
            }}
          >
            {taskTypes.map(type => (
              <MenuItem key={type} value={type}>{type}</MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Priority</InputLabel>
          <Select
            value={formData.priority}
            onChange={(e) => setFormData({...formData, priority: e.target.value})}
          >
            <MenuItem value="HIGH">High</MenuItem>
            <MenuItem value="MEDIUM">Medium</MenuItem>
            <MenuItem value="LOW">Low</MenuItem>
          </Select>
        </FormControl>

        {renderPayloadFields()}

        <Button 
          type="submit" 
          variant="contained" 
          fullWidth 
          sx={{ mt: 3 }}
        >
          Submit Task
        </Button>
      </form>

      {result && (
        <Box sx={{ mt: 2 }}>
          {result.type === 'success' ? (
            <Alert severity="success">
              Task submitted successfully! Task ID: {result.data.taskId}
            </Alert>
          ) : (
            <Alert severity="error">
              Error: {result.message}
            </Alert>
          )}
        </Box>
      )}
    </Paper>
  );
}

export default TaskSubmit;
