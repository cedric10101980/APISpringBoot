db = db.getSiblingDB('mongodb_container');
db.createUser({
  user: "mongo",
  pwd: "mongo",
  roles: [
    { role: "readWrite", db: "mongodb_container" }
  ]
});