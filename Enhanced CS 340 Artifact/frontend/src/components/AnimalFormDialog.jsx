import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  MenuItem,
  CircularProgress
} from '@mui/material';
import { toast } from 'react-toastify';
import animalService from '../services/animalService';

const AnimalFormDialog = ({ open, onClose, animal, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    animal_id: '',
    name: '',
    animal_type: 'Dog',
    breed: '',
    color: '',
    sex_upon_outcome: '',
    age_upon_outcome: '',
    outcome_type: '',
    location_lat: '',
    location_long: ''
  });

  useEffect(() => {
    if (animal) {
      setFormData({
        animal_id: animal.animal_id || '',
        name: animal.name || '',
        animal_type: animal.animal_type || 'Dog',
        breed: animal.breed || '',
        color: animal.color || '',
        sex_upon_outcome: animal.sex_upon_outcome || '',
        age_upon_outcome: animal.age_upon_outcome || '',
        outcome_type: animal.outcome_type || '',
        location_lat: animal.location_lat || '',
        location_long: animal.location_long || ''
      });
    } else {
      setFormData({
        animal_id: '',
        name: '',
        animal_type: 'Dog',
        breed: '',
        color: '',
        sex_upon_outcome: '',
        age_upon_outcome: '',
        outcome_type: '',
        location_lat: '',
        location_long: ''
      });
    }
  }, [animal, open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (animal) {
        // Update existing animal
        await animalService.updateAnimal(animal._id, formData);
        toast.success('Animal updated successfully!');
      } else {
        // Create new animal
        await animalService.createAnimal(formData);
        toast.success('Animal created successfully!');
      }
      onSuccess();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle>
          {animal ? 'Edit Animal' : 'Add New Animal'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Animal ID"
                name="animal_id"
                value={formData.animal_id}
                onChange={handleChange}
                required
                disabled={!!animal}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                select
                label="Animal Type"
                name="animal_type"
                value={formData.animal_type}
                onChange={handleChange}
                required
              >
                <MenuItem value="Dog">Dog</MenuItem>
                <MenuItem value="Cat">Cat</MenuItem>
                <MenuItem value="Other">Other</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Breed"
                name="breed"
                value={formData.breed}
                onChange={handleChange}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Color"
                name="color"
                value={formData.color}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                select
                label="Sex Upon Outcome"
                name="sex_upon_outcome"
                value={formData.sex_upon_outcome}
                onChange={handleChange}
              >
                <MenuItem value="">Select...</MenuItem>
                <MenuItem value="Intact Male">Intact Male</MenuItem>
                <MenuItem value="Neutered Male">Neutered Male</MenuItem>
                <MenuItem value="Intact Female">Intact Female</MenuItem>
                <MenuItem value="Spayed Female">Spayed Female</MenuItem>
                <MenuItem value="Unknown">Unknown</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Age Upon Outcome"
                name="age_upon_outcome"
                value={formData.age_upon_outcome}
                onChange={handleChange}
                placeholder="e.g., 2 years, 6 months"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Outcome Type"
                name="outcome_type"
                value={formData.outcome_type}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Latitude"
                name="location_lat"
                type="number"
                value={formData.location_lat}
                onChange={handleChange}
                inputProps={{ step: "any" }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Longitude"
                name="location_long"
                type="number"
                value={formData.location_long}
                onChange={handleChange}
                inputProps={{ step: "any" }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button 
            type="submit" 
            variant="contained" 
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            {animal ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AnimalFormDialog;
