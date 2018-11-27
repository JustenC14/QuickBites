package quickbites.umflint.com.quickbites;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import quickbites.umflint.com.quickbites.Utilities.DatabaseAccessor;
import quickbites.umflint.com.quickbites.Utilities.MenuListAdapter;
import quickbites.umflint.com.quickbites.Utilities.RecyclerTouchListener;

public class RestaurantMenuManagement extends AppCompatActivity {

    private Button addMenuItem, removeMenuItem;
    private TextView restaurantTitle;
    private RecyclerView menu;
    private RecyclerView.LayoutManager layoutManager;
    private MenuListAdapter menuListAdapter;
    private DatabaseAccessor databaseAccessor;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu_management);
        setTitle("Menu Management");

        final String userID = auth.getUid();
        databaseAccessor = DatabaseAccessor.getInstance();

        Query name_query = databaseAccessor.getDatabaseReference().child("users").child("restaurants").child(userID).child("restaurantName");
        Query menu_query = databaseAccessor.getDatabaseReference().child("menu_items").child(userID);

        addMenuItem = findViewById(R.id.AddMenuItem);
        restaurantTitle = findViewById(R.id.RestaurantTitle);
        menu = findViewById(R.id.MenuCardRecycler);
        menu.setAdapter(menuListAdapter);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        menu.setLayoutManager(layoutManager);

        databaseAccessor.access(false, name_query, new DatabaseAccessor.OnGetDataListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username_snapshot = dataSnapshot.getValue().toString();
                String[] snapshot_array = username_snapshot.split("\\=");
                username_snapshot = snapshot_array[0];
                username_snapshot = username_snapshot + "'s Menu";
                restaurantTitle.setText(username_snapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final List<HashMap<String, String>> menuHashMap = new ArrayList<>();
        databaseAccessor.access(false, menu_query, new DatabaseAccessor.OnGetDataListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,String> current_item;
                HashMap<String, String> current_itemHeader;
                String current_header;

                for(DataSnapshot menu_header : dataSnapshot.getChildren()){
                    current_header = menu_header.getKey();
                    current_itemHeader = new HashMap<>();
                    current_itemHeader.put("item_header", current_header);
                    menuHashMap.add(current_itemHeader);
                    for(DataSnapshot item : menu_header.getChildren()){
                        current_item = (HashMap<String, String>) item.getValue();
                        menuHashMap.add(current_item);
                    }
                }

                menuListAdapter = new MenuListAdapter(menuHashMap);
                menu.setAdapter(menuListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RestaurantMenuManagement.this, AddMenuItem.class));
                finish();
            }
        });

        menu.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), menu, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                String item_to_open = menuHashMap.get(position).get("item_name");;
                String header_to_open = menuHashMap.get(position).get("menu_category");
                //Toast.makeText(getApplicationContext(), "Deleting: " + menuHashMap.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ViewMenuItem.class);
                intent.putExtra("ITEM_NAME", item_to_open);
                intent.putExtra("ITEM_HEADER", header_to_open);
                intent.putExtra("ITEM_OWNER", userID);
                startActivity(intent);
            }
        }));


    }
}
