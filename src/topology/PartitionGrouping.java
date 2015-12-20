package topology;

import backtype.storm.generated.GlobalStreamId;
import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.task.WorkerTopologyContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xianyan Jia on 17/11/2015.
 */
public class PartitionGrouping implements CustomStreamGrouping {
    private List<Integer> _targetTasks;

    @Override
    public void prepare(WorkerTopologyContext workerTopologyContext, GlobalStreamId globalStreamId, List<Integer> list) {
        _targetTasks = list;
        System.out.println("this is the custom grouping");
        for (int i : _targetTasks) {
            System.out.println(i);
        }
    }

    @Override
    public List<Integer> chooseTasks(int i, List<Object> list) {
        ArrayList<Integer> taskids = new ArrayList<>();
        int groupingKey = Integer.valueOf(list.get(1).toString());
        if (groupingKey == -1) {
            return _targetTasks;
        } else {
            if (groupingKey >= Global.N_PARTITION) {
                groupingKey = Global.N_PARTITION - 1;
            }
            System.out.println(groupingKey);
            taskids.add(_targetTasks.get(groupingKey));
        }
        return taskids;
    }
}
