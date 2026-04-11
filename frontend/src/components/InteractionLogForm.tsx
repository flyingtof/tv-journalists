import React, { useState } from 'react';

interface Props {
  onSubmit: (data: { date: string; description: string; activityId?: string }) => void;
  activities: { id: string; mediaName: string }[];
}

export const InteractionLogForm: React.FC<Props> = ({ onSubmit, activities }) => {
  const [formData, setFormData] = useState({
    date: new Date().toISOString().split('T')[0],
    description: '',
    activityId: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      ...formData,
      activityId: formData.activityId || undefined,
    });
    setFormData((prev) => ({ ...prev, description: '' }));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 bg-gray-50 p-4 rounded-lg">
      <h3 className="text-lg font-medium">Log New Interaction</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">Date</label>
          <input
            type="date"
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            required
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Related Activity (Optional)</label>
          <select
            value={formData.activityId}
            onChange={(e) => setFormData({ ...formData, activityId: e.target.value })}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
          >
            <option value="">None</option>
            {activities.map((a) => (
              <option key={a.id} value={a.id}>{a.mediaName}</option>
            ))}
          </select>
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">Description</label>
        <textarea
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          required
          rows={3}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <button
        type="submit"
        className="bg-indigo-600 text-white rounded-md px-4 py-2 hover:bg-indigo-700"
      >
        Log Interaction
      </button>
    </form>
  );
};
