import React, { useState } from 'react';
import { Container, Grid, Box, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';
import { toast } from 'react-toastify';
import Header from './Header';
import FilterPanel from './FilterPanel';
import AnimalTable from './AnimalTable';
import BreedChart from './BreedChart';
import LocationMap from './LocationMap';
import AnimalFormDialog from './AnimalFormDialog';
import useAnimals from '../hooks/useAnimals';
import useAuth from '../hooks/useAuth';
import animalService from '../services/animalService';

const Dashboard = () => {
  const [filterType, setFilterType] = useState('reset');
  const [selectedAnimal, setSelectedAnimal] = useState(null);
  const [displayedAnimals, setDisplayedAnimals] = useState([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editAnimal, setEditAnimal] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [animalToDelete, setAnimalToDelete] = useState(null);
  const { animals, loading, error, refetch } = useAnimals(filterType);
  const { isAdmin } = useAuth();

  const handleFilterChange = (newFilter) => {
    setFilterType(newFilter);
    setSelectedAnimal(null); // Reset selection when filter changes
  };

  const handleSelectAnimal = (animal) => {
    setSelectedAnimal(animal);
  };

  const handlePageChange = (pageAnimals) => {
    setDisplayedAnimals(pageAnimals);
  };

  const handleEditAnimal = (animal) => {
    setEditAnimal(animal);
    setFormOpen(true);
  };

  const handleDeleteAnimal = (animal) => {
    setAnimalToDelete(animal);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    try {
      await animalService.deleteAnimal(animalToDelete._id);
      toast.success('Animal deleted successfully!');
      refetch();
      setDeleteDialogOpen(false);
      setAnimalToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete animal');
    }
  };

  const handleFormSuccess = () => {
    refetch();
  };

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Header />

      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Filter Panel */}
        <FilterPanel filterType={filterType} onFilterChange={handleFilterChange} />

        {/* Animal Table */}
        <Box sx={{ mb: 3 }}>
          <AnimalTable
            animals={animals}
            loading={loading}
            selectedAnimal={selectedAnimal}
            onSelectAnimal={handleSelectAnimal}
            onPageChange={handlePageChange}
            onEdit={handleEditAnimal}
            onDelete={handleDeleteAnimal}
            isAdmin={isAdmin}
          />
        </Box>

        {/* Charts Section */}
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <BreedChart />
          </Grid>
          <Grid item xs={12} md={6}>
            <LocationMap animals={displayedAnimals} selectedAnimal={selectedAnimal} />
          </Grid>
        </Grid>

        {/* Footer */}
        <Box sx={{ mt: 4, textAlign: 'center', color: 'text.secondary' }}>
          <p>CS-499 Capstone Project - Enhanced CS 340 Artifact</p>
          <p>Created by: Eriko Maledhi | Southern New Hampshire University</p>
        </Box>
      </Container>

      {/* Animal Form Dialog */}
      <AnimalFormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        animal={editAnimal}
        onSuccess={handleFormSuccess}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete {animalToDelete?.name || 'this animal'}? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={confirmDelete} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Dashboard;
