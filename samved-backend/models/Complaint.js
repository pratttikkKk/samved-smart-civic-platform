const mongoose = require("mongoose");

const complaintSchema = new mongoose.Schema({
  userId: { type: String, required: true },

  issueType: { type: String, required: true },

  description: { type: String, required: true },

  ward: { type: String, required: true },

  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },

  addressText: { type: String, required: true },

  reporters: {
    type: [String],
    default: []
  },

  priorityScore: {
    type: Number,
    default: 0
  },

  beforePhoto: { type: String, required: true },
  afterPhoto: { type: String },

  status: {
    type: String,
    enum: ["Pending", "In Progress", "Resolved"], // 🔥 FIX
    default: "Pending"
  }

}, { timestamps: true });

module.exports = mongoose.model("Complaint", complaintSchema);
