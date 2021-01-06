CREATE TABLE "person"
(
  "person_id" serial PRIMARY KEY,
  "person_number" varchar(12) UNIQUE,
  "name" varchar(100),
  "street" varchar(100),
  "zip" varchar(5),
  "city" varchar(50),
  "age" int
);

CREATE TABLE "contact_details"
(
  "person_id" int NOT NULL REFERENCES "person" ON DELETE CASCADE,
  "contact" varchar(100) NOT NULL,
  PRIMARY KEY ("person_id")
);

CREATE TABLE "genres"
(
	"genre_id" serial PRIMARY KEY,
    "genre" varchar(100) UNIQUE
);

CREATE TABLE "instruments"
(
	"instrument_name" varchar(100) PRIMARY KEY,
);

CREATE TABLE "levels"
(
	"level" varchar(100) PRIMARY KEY
);

CREATE TABLE "extra_costs"
(
	"costs_id" serial PRIMARY KEY,
    "date_fee" int,
    "rent_cost" int
);

CREATE TABLE "student"
(
  "student_id" serial PRIMARY KEY,
  "person_id" serial NOT NULL REFERENCES "person" UNIQUE,
  "siblings" BIT
);

CREATE TABLE "contact_details_parents"
(
  "student_id" serial NOT NULL REFERENCES "student" ON DELETE CASCADE,
  "contact" varchar(100) NOT NULL,
  PRIMARY KEY ("student_id")
);


CREATE TABLE "instructor"
(
  "instructor_id" serial PRIMARY KEY,
  "person_id" serial NOT NULL REFERENCES "person" UNIQUE,
  "employment_id" varchar(10) UNIQUE NOT NULL
);

CREATE TABLE "teach_ensemble"
(
  "genre_id" serial NOT NULL REFERENCES "genres" ON DELETE CASCADE,
  "instructor_id" serial NOT NULL REFERENCES "instructor" ON DELETE CASCADE,
  PRIMARY KEY("genre_id", "instructor_id")
);

CREATE TABLE "teach_instrument"
(	
  "instrument_id" varchar(100) NOT NULL REFERENCES "instruments" ON DELETE CASCADE,
  "instructor_id" serial NOT NULL REFERENCES "instructor" ON DELETE CASCADE,
  PRIMARY KEY("instrument_id", "instructor_id")
);

CREATE TABLE "renting_instrument"
(
	"renting_id" serial PRIMARY KEY,
    "available_instrument_amount" int ,
	"instrument_type" varchar(100) REFERENCES "instruments" ON DELETE SET NULL,
	"instrument_name" varchar(100) UNIQUE,
	"rental_cost" int,
    "instrument_type" varchar(100) REFERENCES "instruments" ON DELETE SET NULL
);

CREATE TABLE "rented_instrument"
(
	"rented_id" serial PRIMARY KEY,
	"instrument_id" serial NOT NULL REFERENCES "renting_instrument" ON DELETE CASCADE,
	"student_id" serial NOT NULL REFERENCES "student" ON DELETE CASCADE,
    "date" date,
	"currently_renting" bit
);

CREATE TABLE "lesson"
(
	"lesson_id" serial PRIMARY KEY,
    "level" varchar(100) REFERENCES "levels" ON DELETE SET NULL
	"instructor_id" serial REFERENCES "instructor" ON DELETE SET NULL
);

CREATE TABLE "individual_lesson"
(
	"lesson_id" serial REFERENCES "lesson" ON DELETE CASCADE,
    PRIMARY KEY ("lesson_id"),
    "instrument_id" varchar(100) REFERENCES "instruments" ON DELETE SET NULL
);

CREATE TABLE "group_lesson"
(
	"lesson_id" serial REFERENCES "lesson" ON DELETE CASCADE,
    PRIMARY KEY ("lesson_id"),
    "minimum_students" int,
    "maximum_students" int,
    "instrument_id" varchar(100) REFERENCES "instruments" ON DELETE SET NULL
);

CREATE TABLE "ensemble"
(
	"lesson_id" serial REFERENCES "lesson" ON DELETE CASCADE,
    PRIMARY KEY ("lesson_id"),
    "minimum_students" int,
    "maximum_students" int,
    "genre_id" serial REFERENCES "genres" ON DELETE SET NULL
);

CREATE TABLE "student_list"
(
	"list_id" serial PRIMARY KEY,
	"lesson_id" serial REFERENCES "lesson" ON DELETE CASCADE,
   	"student_id" serial REFERENCES "student" ON DELETE CASCADE,
	UNIQUE ("lesson_id","student_id")	
);

CREATE TABLE "application"
(
	"lesson_id" serial REFERENCES "lesson" ON DELETE CASCADE,
	"student_id" serial REFERENCES "student" ON DELETE CASCADE,
	PRIMARY KEY ("lesson_id","student_id"),
    "skill" varchar(500),
    "audition_required" bit,
    "save_application" bit
);

CREATE TABLE "timeslot"
(
	"timeslot_id" serial PRIMARY KEY,
    "lesson_id" serial REFERENCES "lesson" ON DELETE SET NULL,
    "date" date
);

CREATE TABLE "available_instructor"
(
	"instructor_id" serial REFERENCES "instructor" ON DELETE CASCADE,
	"timeslot_id" serial REFERENCES "timeslot" ON DELETE CASCADE,
	PRIMARY KEY ("instructor_id","timeslot_id"),
    "instructor_available" bit
);

CREATE TABLE "lesson_costs"
(
	"level_id" varchar(100) REFERENCES "levels" ON DELETE CASCADE,
	"lesson_cost" int
);

CREATE TABLE "instructor_payment"
(
	"instructor_id" serial REFERENCES "instructor" ON DELETE CASCADE,
	"timeslot_id" serial REFERENCES "timeslot" ON DELETE CASCADE,

	PRIMARY KEY ("instructor_id", "timeslot_id"),
    "payment" int,
    "cost_id" serial REFERENCES "extra_costs" ON DELETE SET NULL,
	"timeslot_id" serial REFERENCES "timeslot" ON DELETE SET NULL,
    "lesson_cost" varchar(100) REFERENCES "lesson_cost" ON DELETE SET NULL
);


CREATE TABLE "student_payment"
(
	"student_id" serial REFERENCES "student" ON DELETE CASCADE,
    "timeslot_id" serial REFERENCES "timeslot" ON DELETE CASCADE,

	PRIMARY KEY ("student_id","timeslot_id"),
    "costs_of_lessons" int,
    "discount_siblings" float,
    "cost_id" serial REFERENCES "extra_costs" ON DELETE SET NULL,
	"timeslot_id" serial REFERENCES "timeslot" ON DELETE SET NULL,
    "lesson_cost" varchar(100) REFERENCES "lesson_cost" ON DELETE SET NULL
);


