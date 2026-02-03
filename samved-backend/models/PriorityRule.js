const mongoose = require("mongoose");

const priorityRuleSchema = new mongoose.Schema({
  areaType: String,
  areaWeight: Number,
  issueType: String,
  issueWeight: Number,
  repeatWeight: Number
});

module.exports = mongoose.model("PriorityRule", priorityRuleSchema);
