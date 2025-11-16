import React, { useMemo } from 'react';
import { Paper, Typography, Box } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const COLORS = [
  '#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8',
  '#82CA9D', '#FFC658', '#FF6B9D', '#9C27B0', '#4CAF50'
];

const BreedChart = ({ animals }) => {
  const chartData = useMemo(() => {
    if (!animals || animals.length === 0) return [];

    // Count breeds
    const breedCounts = {};
    animals.forEach((animal) => {
      const breed = animal.breed || 'Unknown';
      breedCounts[breed] = (breedCounts[breed] || 0) + 1;
    });

    // Convert to array and sort by count
    const data = Object.entries(breedCounts)
      .map(([breed, count]) => ({
        name: breed.length > 25 ? breed.substring(0, 22) + '...' : breed,
        fullName: breed,
        value: count,
        percentage: 0
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10); // Top 10 breeds

    // Calculate percentages
    const total = animals.length;
    data.forEach(item => {
      item.percentage = ((item.value / total) * 100).toFixed(1);
    });

    return data;
  }, [animals]);

  if (!chartData || chartData.length === 0) {
    return (
      <Paper elevation={2} sx={{ p: 3, height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography color="text.secondary">No data available for chart</Typography>
      </Paper>
    );
  }

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <Paper sx={{ p: 1.5, border: '1px solid #ccc' }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {payload[0].payload.fullName}
          </Typography>
          <Typography variant="body2" color="primary">
            Count: {payload[0].value}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {payload[0].payload.percentage}%
          </Typography>
        </Paper>
      );
    }
    return null;
  };

  return (
    <Paper elevation={2} sx={{ p: 3, height: 400 }}>
      <Typography variant="h6" gutterBottom>
        Top 10 Breed Distribution
      </Typography>
      <ResponsiveContainer width="100%" height="90%">
        <BarChart 
          data={chartData}
          margin={{ top: 5, right: 30, left: 20, bottom: 80 }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="name" 
            angle={-45}
            textAnchor="end"
            height={100}
            interval={0}
            style={{ fontSize: '11px' }}
          />
          <YAxis 
            label={{ value: 'Number of Animals', angle: -90, position: 'insideLeft' }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="value" radius={[8, 8, 0, 0]}>
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </Paper>
  );
};

export default BreedChart;
