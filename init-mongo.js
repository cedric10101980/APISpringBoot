db = db.getSiblingDB('mongodb_container');
db.createUser({
  user: "mongo",
  pwd: "mon**",
  roles: [
    { role: "readWrite", db: "mongodb_container" }
  ]
});
