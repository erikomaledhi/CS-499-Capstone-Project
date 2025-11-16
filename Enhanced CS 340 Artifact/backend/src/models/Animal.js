const mongoose = require('mongoose');

/**
 * Animal Schema for MongoDB
 * Represents animal records from the AAC shelter
 */
const animalSchema = new mongoose.Schema(
  {
    rec_num: {
      type: String,
      required: false,
      index: true
    },
    age_upon_outcome: {
      type: String,
      required: false
    },
    age_upon_outcome_in_weeks: {
      type: Number,
      required: true,
      min: 0,
      index: true
    },
    animal_id: {
      type: String,
      required: true,
      unique: true,
      index: true
    },
    animal_type: {
      type: String,
      required: true,
      enum: ['Dog', 'Cat', 'Other'],
      index: true
    },
    breed: {
      type: String,
      required: true,
      index: true
    },
    color: {
      type: String,
      required: false
    },
    date_of_birth: {
      type: Date,
      required: false
    },
    datetime: {
      type: Date,
      required: true,
      index: true
    },
    monthyear: {
      type: String,
      required: false
    },
    name: {
      type: String,
      required: false,
      default: 'Unnamed'
    },
    outcome_subtype: {
      type: String,
      required: false
    },
    outcome_type: {
      type: String,
      required: false,
      index: true
    },
    sex_upon_outcome: {
      type: String,
      required: true,
      enum: [
        'Intact Male',
        'Intact Female',
        'Neutered Male',
        'Spayed Female',
        'Unknown'
      ],
      index: true
    },
    location_lat: {
      type: Number,
      required: true,
      min: -90,
      max: 90
    },
    location_long: {
      type: Number,
      required: true,
      min: -180,
      max: 180
    }
  },
  {
    timestamps: true,
    collection: 'animals'
  }
);

// Compound index for common queries
animalSchema.index({ animal_type: 1, breed: 1 });
animalSchema.index({ animal_type: 1, sex_upon_outcome: 1, age_upon_outcome_in_weeks: 1 });
animalSchema.index({ location_lat: 1, location_long: 1 });

// Virtual for full location
animalSchema.virtual('location').get(function () {
  return {
    lat: this.location_lat,
    lng: this.location_long
  };
});

// Ensure virtuals are included in JSON
animalSchema.set('toJSON', { virtuals: true });
animalSchema.set('toObject', { virtuals: true });

/**
 * Static methods for common queries
 */

// Get animals by rescue type
animalSchema.statics.getWaterRescueDogs = function () {
  return this.find({
    animal_type: 'Dog',
    sex_upon_outcome: /Intact Female/i,
    age_upon_outcome_in_weeks: { $gte: 26, $lte: 156 },
    breed: {
      $in: [
        /Labrador Retriever/i,
        /Chesapeake Bay Retriever/i,
        /Newfoundland/i
      ]
    }
  });
};

animalSchema.statics.getMountainRescueDogs = function () {
  return this.find({
    animal_type: 'Dog',
    sex_upon_outcome: /Intact Male/i,
    age_upon_outcome_in_weeks: { $gte: 26, $lte: 156 },
    breed: {
      $in: [
        /German Shepherd/i,
        /Alaskan Malamute/i,
        /Old English Sheepdog/i,
        /Siberian Husky/i,
        /Rottweiler/i
      ]
    }
  });
};

animalSchema.statics.getDisasterRescueDogs = function () {
  return this.find({
    animal_type: 'Dog',
    sex_upon_outcome: /Intact Male/i,
    age_upon_outcome_in_weeks: { $gte: 20, $lte: 300 },
    breed: {
      $in: [
        /Doberman Pinscher/i,
        /German Shepherd/i,
        /Golden Retriever/i,
        /Bloodhound/i,
        /Rottweiler/i
      ]
    }
  });
};

const Animal = mongoose.model('Animal', animalSchema);

module.exports = Animal;
