
    graph TD;
    
    id1("Creating new tracker")
    id2{"One value per particle?"}

    id3(AbstractTracker)
    id4("<i>track()<br>addRuntimeAttributes()<br>setDimensions()<br>createArray()</i>")
    
    id5(FloatTracker<br>IntegerTracker)
    id6("<i>getValue()</i>")
    
    id1 --> id2
    id2 -->|No| id3
    id2 -->|Yes| id5
    
    id3 --> id4
    id5 --> id6