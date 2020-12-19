/**
 * This class describes an NFA and several useful methods for working with
 * NFAs, including a method that determines whether or not an NFA is a DFA, 
 * and a method capable of minimizing DFAs. 
 * @author Kira Toal
 */ 

package main.nfa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class NFA {
    
    // Fields
    // ------------------------------------------------------------------------

    private HashSet<Character> alphabet; 
    private HashMap<State, HashMap<Character, HashSet<State>>> data;
    
    // Constructor
    // ------------------------------------------------------------------------
    
    public NFA(HashSet<Character> alphabet, HashMap<State, HashMap<Character, 
            HashSet<State>>> data) {
        this.alphabet = alphabet;
        this.data = data;
    }
    
    // Getters
    // ------------------------------------------------------------------------
    
    public HashMap<State, HashMap<Character, HashSet<State>>> getData() {
        return data;
    }

    public HashSet<Character> getAlphabet() {
        return alphabet;
    }
    
    // Methods
    // ------------------------------------------------------------------------
    
    /**
     * Adds a state to the NFA as long as there is not already a state with the
     * same name and acceptance value.
     * @param state The state to add.
     */
    public void addState(State state) {
        if (!this.getData().containsKey(state)) {
            this.getData().put(state, new HashMap<Character, HashSet<State>>());         
        }
    }   
    
    /**
     * Adds a transition to the NFA as long as it there does not already exist
     * a transition that uses the specified symbol.
     * @param state The state to add the transition to (the start state of the
     *      transition.)
     * @param transition The transition itself, which consists of a symbol and 
     *      a destination.
     */
    public void addTransition(State state, Transition transition) {
        // Ensure symbol in alphabet. 
        if (!this.getAlphabet().contains(transition.symbol)) {
            throw new IllegalArgumentException("Transition symbol not in alphabet.");                         
        }
        // Ensure state in NFA. 
        if (!this.getData().containsKey(state)) {
            throw new IllegalArgumentException("Cannot add tranistion to nonexistant state.");             
        }
        // Add new transition. 
        if (!this.getData().get(state).containsKey(transition.symbol)) {
            this.getData().get(state).put(transition.symbol, 
                    new HashSet<State>(Arrays.asList(transition.dest)));
        }
        // Add state to transition.
        else {
            // Do not add duplicate transition. 
            if (this.getData().get(state).get(transition.symbol).contains(transition.dest)) {
                return;
            }
            HashSet<State> oldStatesSet = this.getData().get(state).get(transition.symbol);
            oldStatesSet.add(transition.dest);         
        }                  
    }
    
    /**
     * Determined whether an NFA is a DFA. In order for this to be true,
     * the following conditions must be satisfied: 
     *  (1) The alphabet may not contain λ (no λ transitions allowed)/
     *  (2) Each state must have a transition for every character in the alphabet.
     *  (3) Every symbol must have only one destination state.
     * @return Whether or not a given NFA is also an DFA.
     */
    public boolean isDFA() {
        // Condition 1: DFA cannot use lambda moves. 
        if (this.getAlphabet().contains('λ')) {return false;}
        for (State state: this.getData().keySet()) {
            for (Character c: this.getAlphabet()) {
                // Condition 2: DFA must have transition for every char
                //  in the alphabet. 
                if (!this.getData().get(state).keySet().contains(c)) {
                    return false; 
                }
            }
            for (Character symbol: this.getData().get(state).keySet()) {
                 // Condition 3: Each transition symbol must have only one 
                 //   destination state.
                 if (this.getData().get(state).get(symbol).size() > 1) {
                     return false;
                 }         
             }                   
        }       
        return true;       
    }
    
    /**
     * Minimizes a DFA using the k-equivalence method.
     */
    public NFA minimize() {
        if (!this.isDFA()) {
            throw new IllegalArgumentException("Cannot minimize NFA. Try converting to DFA first.");
        }       
        var sortedGroups = this.kEquivalence(this.getK0());
        var redirect = this.getRedirect(sortedGroups);       
        NFA DFA = new NFA(this.getAlphabet(), new HashMap<State, HashMap<Character, HashSet<State>>>());
        // Build a minimized DFA based on data from the original DFA and the 
        //      state redirects. 
        for (var state: this.getData().keySet()) { // Iterate through original DFA.
            State stateToAdd = redirect.get(state);
            DFA.addState(stateToAdd);          
            // Now add the transitions
            for (var symbol: this.getData().get(state).keySet()) {
                // original transition is 
                State oldDest = this.getData().get(state).get(symbol).toArray(new State[1])[0]; 
                State newDest = redirect.get(oldDest); 
                DFA.addTransition(stateToAdd, new Transition(symbol, newDest));
            }
        }
        return DFA;      
    }     
    
    // General helper methods
    // ------------------------------------------------------------------------

    /**
     * Helper method that prints a visual representation of the NFA in the
     * console.
     */
    public String toString(String title) {
        String result = "--------" + title + "-----------";
        for (State state: this.getData().keySet()) {
            result += "\n" + state.getName() + "\t"; 
            for (Character symbol: this.getData().get(state).keySet()) {
                result += "[" + symbol + ":" + this.transitionSetToString(this.getData().get(state).get(symbol)) + "]";
            }   
        }        
        result += "\n" + "--------------------------------------";
        return result;
    }
    
    /**
     * Returns a string representation of a HashSet of states. Useful
     * for debugging. 
     */
    private String transitionSetToString(HashSet<State> states) {
        String result = ""; 
        for (State state: states) {
            result += " " + state.getName();           
        }
        return result;
    }
    
    /**
     * Prints the k-equivalence groups. This method is useful when
     * working with the minimize() method to see a play-by-play of how
     * it breaks down the states into groups. 
     * Ex: " Groups: [ s0 ] [ s1 ]"
     * Ex2: " Groups: [ s1 s2 s3 ] [ s4 ] [ s0 ]"
     * @param groups
     */
    private void printStateGroup(HashSet<HashSet<State>> groups) {
        String result = "Groups: ";
        for (HashSet<State> group: groups) {
            result += "[ ";
            for (State state: group) {
                result += " " + state.getName() + " ";
            }
            result += " ]";
        }
        System.out.println(result);       
    }
    
    // Helper methods specific to DFA minimization
    // ------------------------------------------------------------------------

    /**
     * Sorts the states of the DFA into groups using the k equivalence method. 
     * @param stateGroups The states should originally be split into two groups:
     *      one for the accept states and one for the non-accept states. 
     * @return The correct sorting of the groups. 
     */
    public HashSet<HashSet<State>> kEquivalence(HashSet<HashSet<State>> stateGroups) {
        var newGroups = new HashSet<HashSet<State>>();
        for (var group: stateGroups) {
            var keep = new HashSet<State>();
            var leave = new HashSet<State>();
            for (var state: group) {
                if (this.stateExits(state, group)) {
                    leave.add(state);
                } else {
                    keep.add(state);
                }
            }
            if (!leave.isEmpty()) {
                newGroups.add(leave);
            }
            if (!keep.isEmpty()) {
                newGroups.add(keep);
            }
        }
        return stateGroups.equals(newGroups) ? newGroups : this.kEquivalence(newGroups);       
    }

    /**
     * Helper method used by minimize() to break the DFA states down into
     * two groups: one including all accept states and the other including
     * all non-accept states. This method provides a necessary starting point
     * for the recursive kEquivalence() method. 
     * @return
     */
    private HashSet<HashSet<State>> getK0() {
        var stateGroups = new HashSet<HashSet<State>>();
        var acceptStates = new HashSet<State>();
        var nonAcceptStates = new HashSet<State>();
        for (State state: this.getData().keySet()) {
            if (state.accept)  {acceptStates.add(state);} 
            else {nonAcceptStates.add(state);}
        }
        stateGroups.add(acceptStates);
        stateGroups.add(nonAcceptStates);
        return stateGroups;
    }

    /**
     * Helper method that determines if a state "exits" its current
     * k-equivalence group, meaning the state contains a transition
     * whose destination is not in the same group as the state.
     * @param state The state to examine.
     * @param group The group the state exists in.
     * @return
     */
    private boolean stateExits(State state, HashSet<State> group) {
        for (Character symbol: this.getData().get(state).keySet()) {
            for (State destination: this.getData().get(state).get(symbol)) {
                if (!group.contains(destination) && group.size() > 1) {                                                        
                    return true;
                }
            }            
        }
        return false;
    }
    
    /**
     * Based on the results of k-equivalence, this helper method maps a state
     * in the original DFA to a its equivalent state in the minimized DFA. 
     * These states may be the same if the original state was unaffected by the
     * minimization operation. 
     * @param sortedGroups
     * @return
     */
    private HashMap<State, State> getRedirect(HashSet<HashSet<State>> sortedGroups) {
        var redirect = new HashMap<State, State>();
        for (var group: sortedGroups) {
            // If the original state was untouched, it can simply be added
            //  into the minimized DFA.
            if (group.size() == 1) {
                State state = group.toArray(new State[1])[0];
                redirect.put(state, state);              
            }
            // Otherwise, we must make a new state. 
            else {
                String aggStateName = "";
                boolean aggStateAccepted = false;
                for (var state: group) {
                    aggStateName += state.getName();
                    if (state.accept) {aggStateAccepted = true;}
                }
                var aggState = new State(aggStateName, aggStateAccepted);
                // Now make all states in this group redirect to the aggregate 
                //      state.
                for (var state: group) {
                    redirect.put(state, aggState);
                }
            }
        } 
        return redirect;
    }
    
    // Subclasses
    // ------------------------------------------------------------------------

    /**
     * The State subclass defines a State object. States include a name and an 
     * accept value (which may be true or false.) States are useful for keeping
     * track of the values in the NFA.data HashMap.
     */
    public static class State {

        private String name;
        private boolean accept;        
                
        public State(String name, boolean accept) {
            this.name = name;
            this.accept = accept;
        }

        public String getName() {
            return name;
        }        
    }
    
    /**
     * The Transition subclass defines a Transition object. Transitions include
     * a symbol Character and a destination State. Transitions are useful for 
     * storing information about the relationship between each state in the NFA.
     */
    public static class Transition {
        
        private Character symbol;
        private State dest; 
        
        public Transition(Character symbol, State dest) {
            this.symbol = symbol; 
            this.dest = dest;
        }       
    }    
}