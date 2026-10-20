package RMI;   // package phai dung "RMI", sai la ClassNotFoundException khi giai ma doi tuong

public class TicketSla implements java.io.Serializable {

    // DUNG con so de cho. Lech 1 chu so -> InvalidClassException: local class incompatible
    private static final long serialVersionUID = 20260517L;

    // DUNG ten va DUNG kieu tung thuoc tinh, thieu mot cai la truong do ve null/0
    private String  ticketId;
    private String  priority;        // LOW, MEDIUM, HIGH, CRITICAL
    private int     openedHoursAgo;
    private boolean breached;
    private String  action;

    public TicketSla() { }           // constructor rong: Java can khi dung lai doi tuong

    public TicketSla(String ticketId, String priority, int openedHoursAgo) {
        this.ticketId = ticketId;
        this.priority = priority;
        this.openedHoursAgo = openedHoursAgo;
    }

    public String  getTicketId()            { return ticketId; }
    public void    setTicketId(String v)    { this.ticketId = v; }
    public String  getPriority()            { return priority; }
    public void    setPriority(String v)    { this.priority = v; }
    public int     getOpenedHoursAgo()      { return openedHoursAgo; }
    public void    setOpenedHoursAgo(int v) { this.openedHoursAgo = v; }
    public boolean isBreached()             { return breached; }   // boolean dung is..., khong phai get...
    public void    setBreached(boolean v)   { this.breached = v; }
    public String  getAction()              { return action; }
    public void    setAction(String v)      { this.action = v; }

    @Override public String toString() {    // chi de in ra xem cho de, khong anh huong cham diem
        return "TicketSla{" + ticketId + ", " + priority + ", " + openedHoursAgo
             + "h, breached=" + breached + ", action=" + action + "}";
    }
}
