graph TD;

    id1("Call to getLayer()")
    id2{"Already called?"}

    id1-->id2

    id3(Create Layer)
    id4(Init. layer)
    id5(Get layer)

    id2 -->|No| id3
    id3 --> id4
    id4 --> id5
        
    id2 -->|Yes| id5