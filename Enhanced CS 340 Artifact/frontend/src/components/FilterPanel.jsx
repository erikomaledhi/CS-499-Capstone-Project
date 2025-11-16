import React from 'react';
import {
  Box,
  Paper,
  Typography,
  ToggleButton,
  ToggleButtonGroup
} from '@mui/material';
import {
  Water,
  Terrain,
  Warning,
  Refresh
} from '@mui/icons-material';

const FilterPanel = ({ filterType, onFilterChange }) => {
  const handleChange = (event, newFilter) => {
    if (newFilter !== null) {
      onFilterChange(newFilter);
    }
  };

  return (
    <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Filter Options
      </Typography>
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
        <ToggleButtonGroup
          value={filterType}
          exclusive
          onChange={handleChange}
          aria-label="rescue type filter"
          color="primary"
        >
          <ToggleButton value="reset" aria-label="show all">
            <Refresh sx={{ mr: 1 }} />
            All Dogs
          </ToggleButton>
          <ToggleButton value="water" aria-label="water rescue">
            <Water sx={{ mr: 1 }} />
            Water Rescue
          </ToggleButton>
          <ToggleButton value="mountain" aria-label="mountain rescue">
            <Terrain sx={{ mr: 1 }} />
            Mountain Rescue
          </ToggleButton>
          <ToggleButton value="disaster" aria-label="disaster rescue">
            <Warning sx={{ mr: 1 }} />
            Disaster Rescue
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>
    </Paper>
  );
};

export default FilterPanel;
