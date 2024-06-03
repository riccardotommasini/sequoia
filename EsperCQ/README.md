
# Continuous Querying with the Event Processing Language


### Project Assumptions

The streamer is generic and requires every statement to have either of the following tag annotations as the first
annotation:

- ```@Tag(name="EPL", value="DDL")```
- ```@Tag(name="EPL", value="DML")```

### Streams

The runner assume a single input file ```Input.stream``` which is a CSV.
Input events should be specified as Maps, i.e.,   ```@EventRepresentation(map)```
In the file, the first field is assumed to be the Event Type and the last is the timestamp.
If the file contains a type that is not identified in the DDL statement, the runner reassign it to the first event specified
For example

```sql

@Tag(name="EPL", value="DDL") 
@EventRepresentation(map) 
create schema StreamA (x string, y int);

@Tag(name="EPL", value="DDL") 
@EventRepresentation(map)
create schema StreamB (z string, q int);

```

With the imput file 

```csv
A,19,1000
B,28,2000
B,26,3000
C,20,4000
```

The two events of type B are automatically reassigned at the event A.
Each stream is loaded from a file 

### Table of Content

- 01: Basics Streams
  - Projection and Simple Aggregation
  - Selection
  - Aggregation
    - windowed
    - grouped
    - orderby
- 02 Tables
  - Populate a table
    - insert into
    - into table
  - Consume from a table
- 03: windows
  - length
  - time
  - batch
  - keepall, first,last
  - output
- 04: Joins
  - stream-stram join
    - inner
    - full outer variants
    - unidirectional
  - stream-table join
- 05: Streaming Operators (R2S)
  - Insertion and Deletion Stream
  - Output all and snapshot 

### TODOS Short Term

- [ ] named windows and window composition
- [ ] variant streams
- [ ] EPL Patterns
- [ ] match recognise
- [ ] contexts

### TODOS Long Term

- [ ] query networks
- [ ] dataflow

