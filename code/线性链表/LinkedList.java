package cn.longq.array;

public class LinkedList {

static class Node{
    public int no;
    public String name;
    public String nickName;
    public Node next;

    Node(int no,String name,String nickName){
        this.name =name;
        this.nickName=nickName;
        this.no = no;
    }
    Node(){}
}

    private Node head;

    public static void main(String[] args) {
        LinkedList list = new LinkedList();
        list.add1(new Node(3,"林冲","豹子头"));
        list.add1(new Node(1,"宋江","及时雨"));
        list.add1(new Node(109,"猪肉匠","镇关西"));

        list.add1(new Node(66,"华荣","小李广"));
        list.add1(new Node(4,"鲁智深","花和尚"));
        list.add1(new Node(2,"卢俊义","玉麒麟"));
//        list.show();
//        System.out.println("开始删除");
//        list.deleted(109);
//        list.show();
//        System.out.println("查询");
//        list.select(66);
//        System.out.println("倒序");
//        list.descSelect(list.head.next);
//        System.out.println("倒数n个");
//        list.descIndex(2, list.head.next);
        System.out.println("反转");
        list.reversetList();
    }

    // 尾插法
    public void  add(Node node){
        if (node == null) throw new RuntimeException();
        if (head == null){
            head = new Node();
        }
        Node stamp = head;
        // 第一种
        while (stamp.next != null){
            stamp = stamp.next;
        }
        stamp.next = node;
        // 第二种
/*        while (true){
            if (stamp.next  == null){
                stamp.next = node;
                break;
            }
            stamp = stamp.next;
        }*/
    }

    public void add1(Node node){
        if (node == null) throw new RuntimeException();
        if (head == null){
            head = new Node();
        }
        Node stamp = head.next;
        Node stampEnd = head;
        while (true){
            if (stamp == null){
                stampEnd.next = node;
                break;
            }else if (stamp.no < node.no && stamp.next == null){
                stamp.next = node;
                break;
            }else if(stampEnd == head && stamp.no > node.no){
                node.next = stamp;
                stampEnd.next = node;
                break;
            } else if (stamp.no > node.no && stampEnd.no < node.no ){
                stampEnd.next = node;
                node.next = stamp;
                break;
            }
            stampEnd = stamp;
            stamp = stamp.next;
        }
    }

    public void deleted(int no){
        if (head == null || head.next == null) return;
        Node stamp = head.next;
        Node stamp1 = head;
        while (true){
            if (stamp == null) break;
            if (stamp.no == no){
                stamp1.next = stamp.next;
            }
            stamp1 = stamp;
            stamp= stamp.next;
        }
    }

    public void select(int no){
        if (head ==null || head.next == null) return;
        Node stamp = head.next;
        while (stamp != null){
            if (stamp.no == no){
                System.out.println("[no="+stamp.no + " name="+stamp.name + " nickName="+ stamp.nickName + "]");
            }
            stamp = stamp.next;
        }
    }

    public void show(){
        Node stamp = head.next;
        while (stamp != null){
            System.out.println("[no="+stamp.no + " name="+stamp.name + " nickName="+ stamp.nickName + "]");
            stamp= stamp.next;
        }
    }


    public void descSelect(Node stamp){
        if (stamp == null) return;
        descSelect(stamp.next);
        System.out.println("[no="+stamp.no + " name="+stamp.name + " nickName="+ stamp.nickName + "]");
    }

    public int descIndex(int index,Node stamp){
        int a = 0;
        if (stamp == null) return a;
        a= descIndex(index,stamp.next);
        a++;
        if (index == a)
        System.out.println("[no="+stamp.no + " name="+stamp.name + " nickName="+ stamp.nickName + "]");
        return a;
    }


    public void reversetList(){
        if (head == null || head.next == null || head.next.next == null ) return;
        Node cur = head.next;
        Node next = null;
        Node reverNode = new Node();
        while (cur != null){
            next = cur.next;
            cur.next = reverNode.next;
            reverNode.next =cur;
            cur =next;
        }
         head = reverNode;

        show();




    }




}






