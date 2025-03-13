package org.example;

public class Storage {
    int[] mem = null;

    public void initialise(int length) {
        int actualLen = (length & ~3) + 2;
        if (actualLen < length) {
            actualLen += 4;
        }
        mem = new int[actualLen]; //already throws OutOfMemoryError
        int last = mem.length - 2;
        nodeCreate(0,last,last,true);
        nodeCreate(last,0,0,false);
    }

    public int malloc(int numInts) {
        int ptr = 0;
        int toAlloc = (numInts & ~3) + 2;
        if (toAlloc < numInts) {
            toAlloc += 4;
        } //allocates a multiple of 4
        while (blockGetSpace(ptr) < toAlloc || !nodeGetIsFree(ptr)) {
            ptr = nodeGetFwdPtr(ptr);
            if (ptr <= 0) {
                return -1;
            }
        }
        nodeSetIsFree(ptr,false);

        if (blockGetSpace(ptr) -2 >= toAlloc) {
            //not exact fit, have to add a new node (-2 to make sure new node will fit
            int newNodeLoc = ptr + toAlloc + 2;
            nodeCreate(newNodeLoc, nodeGetFwdPtr(ptr), ptr, true);
            nodeSetBackPtr(nodeGetFwdPtr(ptr), newNodeLoc);
            nodeSetFwdPtr(ptr, newNodeLoc);

        }

        return ptr + 2; //[ptr] and [ptr + 1] are for linked list cells
    }

    public void free(int index) {
        int ptr = index - 2; //index of ptr, rather than data start
        if (isNotDivBy4(ptr)) {
            throw new IllegalArgumentException("Pointer must be 2 more than multiple of 4");
        }
        int fwd = nodeGetFwdPtr(ptr);
        int back = nodeGetBackPtr(ptr);
        nodeSetIsFree(ptr, true);
        //node should be deleted
        //merge with previous
        if (nodeGetIsFree(back)) {
            nodeSetFwdPtr(back,fwd);
            nodeSetBackPtr(fwd,back);

            ptr = back;
        }
        //merge with subsequent
        if (nodeGetIsFree(fwd)) {
            int doubleAhead = nodeGetFwdPtr(fwd);
            nodeSetFwdPtr(ptr, doubleAhead);
            nodeSetBackPtr(doubleAhead, ptr);
        }


    }

    //free bit stores as LSB in [i+1]
    private boolean nodeGetIsFree(int nodeIndex) {
        int nodeInt = mem[nodeIndex + 1];
        return (nodeInt & 1) == 1;
    }

    private void nodeSetIsFree(int nodeIndex, boolean isFree) {
        mem[nodeIndex + 1] = ((mem[nodeIndex + 1] >> 2) << 2) + boolToInt(isFree);
    }

    //ptr is 0 for end node
    //Fwd ptr is in [i+1], points to back ptr
    private int nodeGetFwdPtr(int nodeIndex) {
        int nodeInt = mem[nodeIndex + 1];
        return (nodeInt & ~3);
    }

    private void nodeSetFwdPtr(int nodeIndex, int val) {
        if (isNotDivBy4(nodeIndex)) {
            throw new IllegalArgumentException("nodeIndex must be a multiple of 4");
        }
        mem[nodeIndex + 1] = val + boolToInt(nodeGetIsFree(nodeIndex));
    }

    //Back ptr is in [i], points to back ptr
    private int nodeGetBackPtr(int nodeIndex) {
        int nodeInt = mem[nodeIndex];
        return (nodeInt & ~3);
    }

    private void nodeSetBackPtr(int nodeIndex, int val) {
        if (isNotDivBy4(nodeIndex)) {
            throw new IllegalArgumentException("nodeIndex must be a multiple of 4");
        }
        mem[nodeIndex] = val + boolToInt(nodeGetIsFree(nodeIndex));
    }

    private void nodeCreate(int nodeIndex, int fwdPtr, int backPtr, boolean free) {
        if (isNotDivBy4(nodeIndex)) {
            throw new IllegalArgumentException("nodeIndex must be a multiple of 4");
        }
        mem[nodeIndex] = backPtr;
        mem[nodeIndex + 1] = fwdPtr + boolToInt(free);
    }

    private int blockGetSpace(int nodeIndex) {
        int nextNode = nodeGetFwdPtr(nodeIndex);
        return nextNode - nodeIndex - 2; // one fwd and one back ptr
    }

    private int boolToInt(boolean b) {
        return b ? 1 : 0;
    }
    private boolean isNotDivBy4(int x) {
        return (x & 3) != 0;
    }

    public int[][] export() {
        //metadata:
        //0: normal
        //1: back ptr
        //2: forwards ptr & busy
        //3: forward ptr & free
        //4: Store, busy
        //5: Store, free
        int[][] toReturn = new int[2][]; // toReturn[0] = mem, toReturn[1] = metadata
        toReturn[0] = mem;
        toReturn[1] = new int[mem.length];
        int ptr = 0;
        do  {
            int isFree = boolToInt(nodeGetIsFree(ptr));
            toReturn[1][ptr] = 1; //back ptr
            toReturn[1][ptr + 1] = 2 + isFree;
            for (int i = ptr + 2; i < nodeGetFwdPtr(ptr); i++) {
                toReturn[1][i] = 4 + isFree;
            }
            ptr = nodeGetFwdPtr(ptr);
        } while (ptr > 0);

        return toReturn;
    }
}
