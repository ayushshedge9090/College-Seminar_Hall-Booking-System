
import java.util.*;

public class SeminarHallReservation {

    static Scanner sc = new Scanner(System.in);

    static int totalSeats, available, n;
    static String[] groups;
    static int[] max, allocation, need, pendingSeats;

    static Queue<Request> waitingList = new LinkedList<>();

    static class Request {
        int group, seats;

        Request(int group, int seats) {
            this.group = group;
            this.seats = seats;
        }
    }

    // Banker's Algorithm: find a safe sequence
    static int[] getSafeSequence() {
        int work = available;
        boolean[] finish = new boolean[n];
        int[] sequence = new int[n];
        int count = 0;

        while (count < n) {
            boolean found = false;

            for (int i = 0; i < n; i++) {
                if (!finish[i] && need[i] <= work) {
                    work += allocation[i];
                    finish[i] = true;
                    sequence[count++] = i;
                    found = true;
                }
            }

            if (!found) {
                return null;
            }
        }

        return sequence;
    }

    // Check a request by temporarily allocating seats
    static boolean isSafeRequest(int g, int seats) {
        available -= seats;
        allocation[g] += seats;
        need[g] -= seats;

        boolean safe = getSafeSequence() != null;

        // Restore temporary allocation
        available += seats;
        allocation[g] -= seats;
        need[g] += seats;

        return safe;
    }

    // Display the safe/unsafe dashboard
    static void dashboard() {
        System.out.println("\n========== SMARTSEM DASHBOARD ==========");
        System.out.println("Total hall seats : " + totalSeats);
        System.out.println("Available seats  : " + available);

        System.out.println("\nGroup\tAllocated\tMaximum\tNeed");

        for (int i = 0; i < n; i++) {
            System.out.println(groups[i] + "\t"
                    + allocation[i] + "\t\t"
                    + max[i] + "\t" + need[i]);
        }

        int[] seq = getSafeSequence();

        if (seq != null) {
            System.out.print("\nSystem Status: SAFE\nSafe Sequence: ");

            for (int i : seq) {
                System.out.print(groups[i] + " ");
            }

            System.out.println();
        } else {
            System.out.println("\nSystem Status: UNSAFE");
        }

        System.out.println("Waiting requests: " + waitingList.size());
        System.out.println("========================================");
    }

    // Process the waiting list in FIFO order
    static void processWaitingList() {
        if (waitingList.isEmpty()) {
            return;
        }

        System.out.println("\nChecking waiting list...");

        int size = waitingList.size();

        for (int k = 0; k < size; k++) {
            Request r = waitingList.poll();
            int g = r.group;

            // Temporarily remove this request's pending claim
            pendingSeats[g] -= r.seats;

            if (r.seats <= available
                    && isSafeRequest(g, r.seats)) {

                available -= r.seats;
                allocation[g] += r.seats;
                need[g] -= r.seats;

                System.out.println("WAITING REQUEST APPROVED: "
                        + groups[g] + " - " + r.seats + " seats");
            } else {
                // Keep the request in the queue
                pendingSeats[g] += r.seats;
                waitingList.offer(r);
            }
        }

        if (waitingList.isEmpty()) {
            System.out.println("Waiting list is empty.");
        }
    }

    // Request a new reservation
    static void bookSeats() {
        System.out.print("Enter group number (1-" + n + "): ");
        int g = sc.nextInt() - 1;

        if (g < 0 || g >= n) {
            System.out.println("Invalid group number!");
            return;
        }

        System.out.print("Enter seats to book: ");
        int seats = sc.nextInt();

        if (seats <= 0 || seats > need[g] - pendingSeats[g]) {
            System.out.println("Invalid request! Check remaining need.");
            return;
        }

        // Respect requests already waiting in the queue
        if (!waitingList.isEmpty()) {
            waitingList.offer(new Request(g, seats));
            pendingSeats[g] += seats;

            System.out.println("Added to waiting list (FIFO).");
            return;
        }

        if (seats > available) {
            waitingList.offer(new Request(g, seats));
            pendingSeats[g] += seats;

            System.out.println("Not enough seats. Added to waiting list.");
            return;
        }

        if (isSafeRequest(g, seats)) {
            available -= seats;
            allocation[g] += seats;
            need[g] -= seats;

            System.out.println("BOOKING APPROVED!");
            System.out.println("Banker's Algorithm: SAFE STATE.");
        } else {
            waitingList.offer(new Request(g, seats));
            pendingSeats[g] += seats;

            System.out.println("Request would cause an unsafe state.");
            System.out.println("Added to waiting list.");
        }
    }

    // Cancel a booking and release seats
    static void cancelBooking() {
        System.out.print("Enter group number (1-" + n + "): ");
        int g = sc.nextInt() - 1;

        if (g < 0 || g >= n) {
            System.out.println("Invalid group number!");
            return;
        }

        if (allocation[g] == 0) {
            System.out.println("This group has no seats to release.");
            return;
        }

        System.out.println("Currently booked seats: " + allocation[g]);
        System.out.print("Enter seats to cancel: ");
        int seats = sc.nextInt();

        if (seats <= 0 || seats > allocation[g]) {
            System.out.println("Invalid cancellation!");
            return;
        }

        allocation[g] -= seats;
        need[g] += seats;
        available += seats;

        System.out.println("CANCELLATION SUCCESSFUL!");
        System.out.println(seats + " seats released.");

        processWaitingList();
    }

    // Display waiting requests
    static void showWaitingList() {
        System.out.println("\n========== WAITING LIST ==========");

        if (waitingList.isEmpty()) {
            System.out.println("No pending requests.");
        } else {
            for (Request r : waitingList) {
                System.out.println(groups[r.group]
                        + " requested " + r.seats + " seats");
            }
        }

        System.out.println("==================================");
    }

    public static void main(String[] args) {

        System.out.println("==================================");
        System.out.println(" SMARTSEM - COLLEGE HALL SYSTEM");
        System.out.println(" Banker's Algorithm Based");
        System.out.println("==================================");

        System.out.print("Enter total seminar hall seats: ");
        totalSeats = sc.nextInt();

        System.out.print("Enter number of student groups: ");
        n = sc.nextInt();

        if (totalSeats <= 0 || n <= 0) {
            System.out.println("Invalid input!");
            sc.close();
            return;
        }

        groups = new String[n];
        max = new int[n];
        allocation = new int[n];
        need = new int[n];
        pendingSeats = new int[n];

        for (int i = 0; i < n; i++) {
            sc.nextLine();

            System.out.print("Enter group/department name " + (i + 1) + ": ");
            groups[i] = sc.nextLine();

            System.out.print("Maximum seats needed by "
                    + groups[i] + ": ");
            max[i] = sc.nextInt();

            if (max[i] < 0 || max[i] > totalSeats) {
                System.out.println("Invalid maximum seat requirement!");
                sc.close();
                return;
            }

            // Initially, no seats are booked
            allocation[i] = 0;
            need[i] = max[i];
        }

        available = totalSeats;

        int choice;

        do {
            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Book Seminar Hall Seats");
            System.out.println("2. Cancel Booking");
            System.out.println("3. View Safe/Unsafe Dashboard");
            System.out.println("4. View Waiting List");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    bookSeats();
                    break;

                case 2:
                    cancelBooking();
                    break;

                case 3:
                    dashboard();
                    break;

                case 4:
                    showWaitingList();
                    break;

                case 5:
                    System.out.println("Thank you for using SmartSem!");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 5);

        sc.close();
    }
}
