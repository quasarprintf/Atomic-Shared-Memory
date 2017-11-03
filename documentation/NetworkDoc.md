### This will be more organized the deeper we delve into the project

### Server-Client communication
    The messages sent over the network from client to server will be formatted as follows: (without angled brackets)
        "read" request    : <reqid>:<flag>:<pcid>:<key>
          * reqid : the hexadecimal local timestamp of the client that sent the request to keep track of which requests are outdated stored
          * flag  : the String value showing message type; this would be "read-request"
          * pcid  : the 8 bit hexidecimal id of the machine that sent the message (the client machine); higher value takes priority in ties
          * key   : the String identifier used to find the information the client is looking for
        "read" returns    : <reqid>:<flag>:<pcid>:<seqid>:<val>
          * reqid : the hexadecimal local timestamp of the client that sent the request to keep track of which requests are outdated
          * flag  : the String showing message type; this would be "read-return"
          * pcid  : the 8 bit hexadecimal id of the machine that sent the message (the server machine); higher value takes priority in ties
          * val   : the String value to be returned
          * seqid : the 8 bit hexadecimal value that identifies how fresh this value is; where higher values are fresher
        "write" request   : <reqid>:<flag>:<pcid>:<seqid>:<key>:<val>:
          * reqid : the hexadecimal local timestamp of the client that sent the request to keep track of which requests are outdated
          * flag  : the String showing message type; this would be "write-request"
          * pcid  : the 8 bit hexadecimal id of the machine that sent the message (the client machine); higher value takes priority in ties
          * key   : the id of where <val> will be written
          * val   : the String value to be written
          * seqid : the 8 bit hexadecimal value that identifies how fresh this value is; higher values are fresher
          
          "write" return  : <reqid>:<flag>:<pcid>:<key>
          * reqid : the hexadecimal local timestamp of the client that sent the request to keep track of which requests are outdated
          * flag  : the String showing message type; this would be "write-return"
          * pcid  : the 8 bit hexadecimal id of the machine that sent the message (the client machine); higher value takes priority in ties
          * key   : the id of where <val> will be written
          
        
    Note that the order of this message is important. If we have a "write" operation, then we know that <val> is a key/value pair.
    If we have a "read" operation, then we know that <val> has only a key and not key/value pair.  We also know that we will not have a
    <sequenceNum> parameter because the sequenceNum value will be compared only after being returned to the client.
    
    
